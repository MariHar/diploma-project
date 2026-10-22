import datetime
import json
import os
import pathlib
import re
import anthropic

# Note: Using the standard Anthropic Claude 3.5 Sonnet model ID
MODEL = 'claude-3-5-sonnet-latest'
BATCH_SIZE = int(os.environ.get('BATCH_SIZE', '3'))
PROGRESS = pathlib.Path('.agent/progress.json')
REPORT = pathlib.Path('AGENT_REPORT.md')

NL = chr(10)
CR = chr(13)
BS = chr(92)
Q = chr(34)
APOS = chr(39)
Q3 = Q * 3

RULES = '''You clean up COMMENTS in Java source files.
You receive one complete Java file. Return it with only the comments
improved.

STRICT RULES
1. Never change code. Do not rename, reformat, reorder, add or remove
anything that is not a comment. Keep imports, annotations, blank lines,
indentation and the spelling of code exactly as they are.
2. Remove commented-out code, empty or meaningless comments, noise (like asdf
or test test), and comments that only repeat the obvious.
3. Keep license headers and comments that explain WHY something is done, non-obvious logic, or important warnings. Keep Javadoc on public classes and
methods, and fix it if it is wrong or unclear.
4. Fix spelling mistakes and make unclear comments short and accurate. Keep
every comment in its original language.
5. If a comment is wrong because the code does something else, correct the
comment to match the code. Never change the code to match the comment.
6. Do not fix bugs. If you notice bugs, security problems or bad practices,
list them in the flaws section.

OUTPUT FORMAT (exactly this, nothing before or after):
<cleaned_file>
the complete file
</cleaned_file>
<flaws>
- line N: the problem and a suggested fix
(or write NONE)
</flaws>'''


def code_only(src):
    # Returns the code with all comments and all whitespace (outside of strings) removed.
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if src.startswith(Q3, i):
            j = i + 3
            while j < n:
                if src[j] == BS:
                    j += 2
                elif src.startswith(Q3, j):
                    j += 3
                    break
                else:
                    j += 1
            out.append(src[i:j])
            i = j
        elif c == Q or c == APOS:
            j = i + 1
            while j < n and src[j] != c:
                j += 2 if src[j] == BS else 1
            out.append(src[i:j + 1])
            i = j + 1
        elif src.startswith('//', i):
            j = src.find(NL, i)
            i = n if j == -1 else j
        elif src.startswith('/*', i):
            j = src.find('*/', i + 2)
            i = n if j == -1 else j + 2
        elif c.isspace():
            i += 1
        else:
            out.append(c)
            i += 1
    return ''.join(out)


def parse_reply(text):
    m = re.search(r'<cleaned_file>(.*?)</cleaned_file>', text, re.S)
    if not m:
        return None, ''
    body = m.group(1)
    if body.startswith(NL):
        body = body[1:]
    f = re.search(r'<flaws>(.*?)</flaws>', text, re.S)
    return body, (f.group(1).strip() if f else '')


def ask_claude(client, source):
    with client.messages.stream(
        model=MODEL,
        max_tokens=16000,
        system=RULES,
        messages=[{'role': 'user', 'content': source}],
    ) as stream:
        message = stream.get_final_message()
    text = ''.join(b.text for b in message.content if b.type == 'text')
    return text, message.stop_reason


def main():
    client = anthropic.Anthropic()
    done = json.loads(PROGRESS.read_text(encoding='utf-8')) if PROGRESS.exists() else {}
    files = sorted(pathlib.Path('src').rglob('*.java'))
    todo = [p for p in files if p.as_posix() not in done][:BATCH_SIZE]

    report = ['# Comment cleanup report ' + str(datetime.date.today()), '']
    if not todo:
        report.append('No Java files left to process.')

    for path in todo:
        key = path.as_posix()
        print('Processing', key)
        flaws = ''
        try:
            original = path.read_bytes().decode('utf-8')
            reply, stop = ask_claude(client, original)
            cleaned, flaws = parse_reply(reply)

            if stop != 'end_turn' or cleaned is None:
                status, note = 'rejected', 'The answer was incomplete, original kept.'
            else:
                uses_crlf = (CR + NL) in original
                cleaned = cleaned.replace(CR + NL, NL).rstrip(NL)
                if original.endswith(NL):
                    cleaned += NL
                if uses_crlf:
                    cleaned = cleaned.replace(NL, CR + NL)

                if code_only(cleaned) != code_only(original):
                    status, note = 'rejected', 'The code would have changed, original kept.'
                elif cleaned == original:
                    status, note = 'unchanged', 'No comment changes were needed.'
                else:
                    path.write_bytes(cleaned.encode('utf-8'))
                    status, note = 'cleaned', 'Comments cleaned, code verified identical.'
        except Exception as error:
            status, note = 'error', str(error)

        report.append('## ' + key)
        report.append('Status: ' + status + '. ' + note)
        if flaws and flaws.strip().upper() != 'NONE':
            report.append('')
            report.append('Possible flaws (nothing was changed for these):')
            report.append(flaws)
        report.append('')

        if status != 'error':
            done[key] = status

    PROGRESS.parent.mkdir(exist_ok=True)
    PROGRESS.write_text(json.dumps(done, indent=2, ensure_ascii=False), encoding='utf-8')
    REPORT.write_text(NL.join(report)[:60000], encoding='utf-8')


if __name__ == '__main__':
    main()

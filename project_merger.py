import os

def merge_project_files(root_dir, output_file):
    # Extensions we want to look at
    valid_extensions = ('.java', '.html', '.css', '.js', '.xml', '.properties', '.yml')
    
    # Directories to completely skip
    ignore_dirs = {'.git', '.idea', 'target', '.mvn', 'node_modules', 'bin', 'obj'}
    
    # Files to skip (like this script itself or the output file)
    ignore_files = {'project_merger.py', output_file, 'mvnw', 'mvnw.cmd'}

    print("🔍 Սկսվում է պրոյեկտի սկանավորումը...")
    file_count = 0

    with open(output_file, 'w', encoding='utf-8') as outfile:
        # Write an initial instruction header for the AI
        outfile.write("=== AI CONTEXT FILE ===\n")
        outfile.write("Այս ֆայլը պարունակում է ամբողջ պրոյեկտի կոդը համակցված տեսքով։\n\n")
        
        for root, dirs, files in os.walk(root_dir):
            # Modify dirs in-place to skip ignored folders completely
            dirs[:] = [d for d in dirs if d not in ignore_dirs]
            
            for file in files:
                if file in ignore_files:
                    continue
                    
                if file.endswith(valid_extensions):
                    file_path = os.path.join(root, file)
                    # Get relative path for clean representation
                    rel_path = os.path.relpath(file_path, root_dir)
                    
                    try:
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as infile:
                            content = infile.read()
                            
                        outfile.write(f"\n{'='*80}\n")
                        outfile.write(f"FILE: {rel_path}\n")
                        outfile.write(f"{'='*80}\n\n")
                        outfile.write(content)
                        outfile.write("\n")
                        
                        print(f"✅ Ավելացվեց: {rel_path}")
                        file_count += 1
                    except Exception as e:
                        print(f"❌ Սխալ {rel_path} ֆայլը կարդալիս: {e}")

    print(f"\n🎉 Ավարտվեց։ {file_count} ֆայլ հաջողությամբ միավորվեց '{output_file}' ֆայլում։")

if __name__ == '__main__':
    # Runs in the current directory where the script is placed
    current_directory = os.path.dirname(os.path.abspath(__file__))
    output_filename = "entire_project_context.txt"
    merge_project_files(current_directory, output_filename)

package com.example.diplomathesisfinalcollection.newaiclaude;

//import com.medical.chat.model.ChatMessage;

//import com.example.aiapi.newaiclaude.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String conversationId;
    private String reply;
    private List<ChatMessage> history;
    private String disclaimer;
}

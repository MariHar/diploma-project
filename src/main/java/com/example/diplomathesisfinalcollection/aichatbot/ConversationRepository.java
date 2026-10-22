package com.example.diplomathesisfinalcollection.aichatbot;


//import com.medical.chat.model.Conversation;

//import com.example.aiapi.newaiclaude.Conversation;
//import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByPatientIdOrderByUpdatedAtDesc(String patientId);

    Optional<Conversation> findByIdAndPatientId(String id, String patientId);
}
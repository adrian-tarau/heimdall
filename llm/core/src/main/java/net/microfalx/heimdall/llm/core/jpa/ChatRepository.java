package net.microfalx.heimdall.llm.core.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("CoreChatRepository")
public interface ChatRepository extends JpaRepository<Chat,Integer> {
}

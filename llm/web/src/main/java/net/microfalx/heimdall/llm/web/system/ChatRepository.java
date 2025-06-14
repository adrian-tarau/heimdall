package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.stereotype.Repository;

@Repository("WebChatRepository")
public interface ChatRepository extends NaturalJpaRepository<Chat, Integer> {
}

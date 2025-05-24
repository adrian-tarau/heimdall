package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.stereotype.Repository;

@Repository("CoreChatRepository")
public interface ChatRepository extends NaturalJpaRepository<Chat,Integer> {
}

package org.jubensha.aijubenshabackend.repository.player;

import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);

    Optional<Player> findByEmail(String email);

    List<Player> findByStatus(PlayerStatus status);

    List<Player> findByRole(PlayerRole role);

    List<Player> findByStatusAndRole(PlayerStatus status, PlayerRole role);

    List<Player> findByNicknameContaining(String nickname);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
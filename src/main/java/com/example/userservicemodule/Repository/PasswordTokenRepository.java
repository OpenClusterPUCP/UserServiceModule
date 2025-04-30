package com.example.userservicemodule.Repository;

import com.example.userservicemodule.Entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD con tokens de restablecimiento de contraseña.
 */
@Repository
public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    /**
     * Busca un token por su valor.
     *
     * @param token El valor del token a buscar
     * @return El token encontrado o un Optional vacío
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina todos los tokens asociados a un usuario.
     *
     * @param userId El ID del usuario
     */
    void deleteByUserId(Integer userId);
}
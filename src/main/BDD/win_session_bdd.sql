CREATE TABLE `win_session` (
                               `id` INT NOT NULL AUTO_INCREMENT,
                               `win_id` INT NOT NULL,
                               `game_session_id` INT NOT NULL,
                               PRIMARY KEY (`id`),
                               FOREIGN KEY (`win_id`) REFERENCES `win`(`id`),
                               FOREIGN KEY (`game_session_id`) REFERENCES `game_session`(`id`)
)

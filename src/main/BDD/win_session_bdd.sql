CREATE TABLE `win_session` (
                               `id` INT NOT NULL AUTO_INCREMENT,
                               `win_id` INT NOT NULL,
                               `session_id` INT NOT NULL,
                               PRIMARY KEY (`id`),
                               FOREIGN KEY (`win_id`) REFERENCES `win`(`id`),
                               FOREIGN KEY (`session_id`) REFERENCES `session`(`id`)
)

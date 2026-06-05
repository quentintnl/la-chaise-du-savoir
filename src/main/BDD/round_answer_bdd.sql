CREATE TABLE `round_answer` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `round_id` INT NOT NULL,
    `player_id` INT NOT NULL,
    `correct_answers` INT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`round_id`) REFERENCES `rounds`(`id`),
    FOREIGN KEY (`player_id`) REFERENCES `user`(`id`)
);


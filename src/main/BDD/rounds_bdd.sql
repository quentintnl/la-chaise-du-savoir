CREATE TABLE `rounds` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `match_id` INT NOT NULL,
    `round_number` INT,
    `total_question` INT,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`match_id`) REFERENCES `game_match`(`id`)
);


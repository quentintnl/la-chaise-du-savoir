CREATE TABLE `game_match` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user1_id` INT NOT NULL,
    `user2_id` INT,
    `invite_code` VARCHAR(4),
    `created_at` DATETIME NOT NULL,
    `status` BOOLEAN,
    `finalized` BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user1_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`user2_id`) REFERENCES `user`(`id`)
);


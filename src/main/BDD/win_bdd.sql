CREATE TABLE `win` (
                       `id` INT NOT NULL AUTO_INCREMENT,
                       `user_id` INT NOT NULL,
                       `points` INT NOT NULL,
                       PRIMARY KEY (`id`),
                       FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

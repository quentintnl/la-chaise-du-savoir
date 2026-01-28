CREATE TABLE `session` (
                           `id` INT NOT NULL AUTO_INCREMENT,
                           `user_id` INT NOT NULL,
                           `api_token` VARCHAR(255) NOT NULL,
                           PRIMARY KEY (`id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

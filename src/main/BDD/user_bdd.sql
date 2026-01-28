CREATE TABLE `user` (
                        `id` INT NOT NULL AUTO_INCREMENT,
                        `login` VARCHAR(50) NOT NULL,
                        `password` VARCHAR(255) NOT NULL,
                        `user_winstreak` INT DEFAULT 0,
                        `global_points` INT DEFAULT 0,
                        PRIMARY KEY (`id`)
);

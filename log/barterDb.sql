
CREATE SCHEMA IF NOT EXISTS `barterplus` DEFAULT CHARACTER SET utf8 ;
USE `barterplus` ;

-- -----------------------------------------------------
-- Table `barterplus`.`participants`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`participants` (
  `player_uuid` VARCHAR(100) NOT NULL,
  `username` VARCHAR(45) NULL,
  `firstName` VARCHAR(45) NULL,
  `lastName` VARCHAR(45) NULL,
  `age` INT NULL,
  PRIMARY KEY (`player_uuid`));


-- -----------------------------------------------------
-- Table `barterplus`.`barterGame`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`barterGame` (
  `num` INT NOT NULL,
  `duration` INT NULL,
  `winner` INT NULL,
  PRIMARY KEY (`num`),
  INDEX `winn_idx` (`winner` ASC) VISIBLE,
  CONSTRAINT `winn`
    FOREIGN KEY (`winner`)
    REFERENCES `barterplus`.`participants` (`player_uuid`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);


-- -----------------------------------------------------
-- Table `barterplus`.`TradeRequest`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`TradeRequest` (
  `RequestID` VARCHAR(100) NOT NULL,
  `requester` VARCHAR(100) NOT NULL,
  `requested` VARCHAR(100) NOT NULL,
  `status` VARCHAR(45) NULL,
  `timeCreated` DOUBLE NULL,
  `timeFinished` DOUBLE NULL,
  `game` INT NULL,
  PRIMARY KEY (`RequestID`),
  INDEX `request_idx` (`requester` ASC, `requested` ASC) VISIBLE,
  INDEX `barterG_idx` (`game` ASC) VISIBLE,
  CONSTRAINT `players`
    FOREIGN KEY (`requester` , `requested`)
    REFERENCES `barterplus`.`participants` (`player_uuid` , `player_uuid`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `barterG`
    FOREIGN KEY (`game`)
    REFERENCES `barterplus`.`barterGame` (`num`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);


-- -----------------------------------------------------
-- Table `barterplus`.`Materials`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`Materials` (
  `Namespace` VARCHAR(30) NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`Namespace`));


-- -----------------------------------------------------
-- Table `barterplus`.`Exchanges`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`Exchanges` (
  `exchange_num` INT NOT NULL AUTO_INCREMENT,
  `TradeRequest_RequestID` VARCHAR(100) NOT NULL,
  `Materials_Namespace` VARCHAR(30) NOT NULL,
  `amount` INT NOT NULL,
  `offerred` TINYINT NULL,
  PRIMARY KEY (`exchange_num`),
  INDEX `fk_Trade_has_Materials_Materials1_idx` (`Materials_Namespace` ASC) VISIBLE,
  INDEX `fk_Trade_has_Materials_TradeRequest1_idx` (`TradeRequest_RequestID` ASC) VISIBLE,
  CONSTRAINT `fk_Trade_has_Materials_Materials1`
    FOREIGN KEY (`Materials_Namespace`)
    REFERENCES `barterplus`.`Materials` (`Namespace`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Trade_has_Materials_TradeRequest1`
    FOREIGN KEY (`TradeRequest_RequestID`)
    REFERENCES `barterplus`.`TradeRequest` (`RequestID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `barterplus`.`profession`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`profession` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `barterplus`.`players`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `barterplus`.`players` (
  `players_uuid` VARCHAR(100) NOT NULL,
  `barterGame_num` INT NOT NULL,
  `profession` INT NULL,
  PRIMARY KEY (`players_uuid`, `barterGame_num`),
  INDEX `fk_players_has_barterGame_barterGame1_idx` (`barterGame_num` ASC) VISIBLE,
  INDEX `fk_players_has_barterGame_players1_idx` (`players_uuid` ASC) VISIBLE,
  INDEX `prof_idx` (`profession` ASC) VISIBLE,
  CONSTRAINT `fk_players_has_barterGame_players1`
    FOREIGN KEY (`players_uuid`)
    REFERENCES `barterplus`.`participants` (`player_uuid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_players_has_barterGame_barterGame1`
    FOREIGN KEY (`barterGame_num`)
    REFERENCES `barterplus`.`barterGame` (`num`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `prof`
    FOREIGN KEY (`profession`)
    REFERENCES `barterplus`.`profession` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);



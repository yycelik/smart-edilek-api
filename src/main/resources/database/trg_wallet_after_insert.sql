DROP TRIGGER IF EXISTS trg_wallet_after_insert;
DELIMITER $$
CREATE TRIGGER trg_wallet_after_insert
AFTER INSERT ON credit_transaction
FOR EACH ROW
BEGIN
    CALL sp_recalculate_wallet(NEW.wallet_id);
END$$
DELIMITER ;

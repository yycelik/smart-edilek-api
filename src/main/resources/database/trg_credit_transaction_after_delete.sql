DROP TRIGGER IF EXISTS trg_credit_transaction_after_delete;
DELIMITER $$
CREATE TRIGGER trg_credit_transaction_after_delete
AFTER DELETE ON credit_transaction
FOR EACH ROW
BEGIN
    CALL sp_recalculate_wallet(OLD.wallet_id);
END$$
DELIMITER ;

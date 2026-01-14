DROP TRIGGER IF EXISTS trg_credit_transaction_after_update;
DELIMITER $$
CREATE TRIGGER trg_credit_transaction_after_update
AFTER UPDATE ON credit_transaction
FOR EACH ROW
BEGIN
    CALL sp_recalculate_wallet(NEW.wallet_id);
    IF OLD.wallet_id <> NEW.wallet_id THEN
        CALL sp_recalculate_wallet(OLD.wallet_id);
    END IF;
END$$
DELIMITER ;

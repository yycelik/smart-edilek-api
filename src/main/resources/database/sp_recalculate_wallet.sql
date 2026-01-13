SHOW TRIGGERS;DELIMITER $$
CREATE DEFINER=`root`@`%` PROCEDURE `sp_recalculate_wallet`(IN p_wallet_id BIGINT)
BEGIN
    DECLARE v_total INT DEFAULT 0;
    DECLARE v_used INT DEFAULT 0;

    -- TOTAL CREDITS (PURCHASE + REFUND + POSITIVE ADJUST)
    SELECT COALESCE(SUM(amount), 0)
      INTO v_total
      FROM credit_transaction
     WHERE wallet_id = p_wallet_id
       AND active = 1
       AND credit_transaction_type_id IN (1, 3, 4);  -- PURCHASE, REFUND, ADJUST

    -- USED CREDITS (SPEND)
    SELECT COALESCE(SUM(amount), 0)
      INTO v_used
      FROM credit_transaction
     WHERE wallet_id = p_wallet_id
       AND active = 1
       AND credit_transaction_type_id = 2;  -- SPEND

    -- UPDATE WALLET
    UPDATE credit_wallet
       SET total_credits     = v_total,
           used_credits      = v_used,
           remaining_credits = v_total - v_used,
           last_calculated_at = NOW()
     WHERE id = p_wallet_id;
END$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS `sp_recalculate_wallet`$$

CREATE DEFINER=`root`@`%` PROCEDURE `sp_recalculate_wallet`(IN p_wallet_id BIGINT)
BEGIN
    DECLARE v_total_additions INT DEFAULT 0;
    DECLARE v_total_expired INT DEFAULT 0;
    DECLARE v_used INT DEFAULT 0;
    DECLARE v_final_total INT DEFAULT 0;
    DECLARE v_remaining INT DEFAULT 0;

    -- 1. ADDITIONS (GELEN KREDİLER)
    -- Purchase(1), Refund(3), Adjust(4)
    SELECT COALESCE(SUM(amount), 0)
      INTO v_total_additions
      FROM credit_transaction
     WHERE wallet_id = p_wallet_id
       AND active = 1
       AND credit_transaction_type_id IN (1, 3, 4);

    -- 2. EXPIRATIONS (SÜRESİ DOLANLAR)
    -- Expired(5)
    SELECT COALESCE(SUM(amount), 0)
      INTO v_total_expired
      FROM credit_transaction
     WHERE wallet_id = p_wallet_id
       AND active = 1
       AND credit_transaction_type_id = 5;

    -- 3. USAGE (HARCAMALAR)
    -- Spend(2)
    SELECT COALESCE(SUM(amount), 0)
      INTO v_used
      FROM credit_transaction
     WHERE wallet_id = p_wallet_id
       AND active = 1
       AND credit_transaction_type_id = 2;

    -- CALCULATION
    -- Net Total = Purchased - Expired
    SET v_final_total = GREATEST(0, v_total_additions - v_total_expired);
    -- Remaining = Total - Used
    SET v_remaining = GREATEST(0, v_final_total - v_used);

    -- UPDATE WALLET
    UPDATE credit_wallet
       SET total_credits     = v_final_total,
           used_credits      = v_used,
           remaining_credits = v_remaining,
           last_calculated_at = NOW()
     WHERE id = p_wallet_id;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_user_order_to_credit_transaction;
DELIMITER $$

CREATE TRIGGER trg_user_order_to_credit_transaction
AFTER UPDATE ON user_order
FOR EACH ROW
BEGIN
    DECLARE v_credit INT DEFAULT 0;
    DECLARE v_wallet_id BIGINT DEFAULT NULL;

    -- SADECE PAID (2), CAMPAIGN (5) VE MANUAL (6) DURUMLARINDA ÇALIŞSIN
    IF NEW.user_order_status_id IN (2, 5, 6)
       AND OLD.user_order_status_id <> NEW.user_order_status_id THEN

        -- PAKETİN VERDİĞİ KREDİYİ AL
        SELECT credit_amount
          INTO v_credit
          FROM license_package
         WHERE id = NEW.license_package_id;

        -- KULLANICININ MEVCUT WALLET ID'SİNİ AL
        SELECT id
          INTO v_wallet_id
          FROM credit_wallet
         WHERE user_id = NEW.user_id
         LIMIT 1;

        -- WALLET YOKSA OLUŞTUR
        IF v_wallet_id IS NULL THEN
            INSERT INTO credit_wallet (
                user_id,
                active,
                created_date,
                total_credits,
                used_credits,
                remaining_credits,
                last_calculated_at
            ) VALUES (
                NEW.user_id,
                1,
                NOW(),
                0,
                0,
                0,
                NOW()
            );

            SET v_wallet_id = LAST_INSERT_ID();
        END IF;

        -- TRANSACTION OLUŞTUR (ID OTO ARTAN)
        INSERT INTO credit_transaction (
            active,
            amount,
            credit_transaction_type_id,
            user_id,
            user_order_id,
            wallet_id,
            created_date
        ) VALUES (
            1,
            v_credit,
            1,  -- PURCHASE = 1 OLDUĞUNU VARSAYDIM, İSTERSEN LOOKUP ID'Yİ SÖYLE
            NEW.user_id,
            NEW.id,
            v_wallet_id,
            NOW()
        );

    END IF;

END$$

DELIMITER ;

DROP TRIGGER IF EXISTS trg_user_order_to_credit_transaction;
DELIMITER $$

CREATE TRIGGER trg_user_order_to_credit_transaction
AFTER UPDATE ON user_order
FOR EACH ROW
BEGIN
    DECLARE v_credit INT DEFAULT 0;
    DECLARE v_wallet_id BIGINT DEFAULT NULL;

    -- CASE 1: NOT PAID -> PAID (Grant Credits)
    IF NEW.user_order_status_id IN (2, 5, 6)
       AND (OLD.user_order_status_id NOT IN (2, 5, 6) OR OLD.user_order_status_id IS NULL) THEN

        -- PAKETİN VERDİĞİ KREDİYİ AL
        SELECT credit_amount
          INTO v_credit
          FROM license_package
         WHERE id = NEW.license_package_id;

        -- WALLET SECIMI (KURUMSAL VEYA BIREYSEL)
        IF NEW.company_id IS NOT NULL THEN
             -- KURUMSAL WALLET ARA (User ID NULL)
             SELECT id INTO v_wallet_id 
               FROM credit_wallet 
              WHERE company_id = NEW.company_id 
                AND user_id IS NULL 
              LIMIT 1;

             -- WALLET YOKSA OLUŞTUR (KURUMSAL)
             IF v_wallet_id IS NULL THEN
                INSERT INTO credit_wallet (
                    user_id,
                    company_id,
                    active,
                    created_date,
                    total_credits,
                    used_credits,
                    remaining_credits,
                    last_calculated_at
                ) VALUES (
                    NULL,
                    NEW.company_id,
                    1,
                    NOW(),
                    0,
                    0,
                    0,
                    NOW()
                );
                SET v_wallet_id = LAST_INSERT_ID();
             END IF;
        ELSE
             -- BIREYSEL WALLET ARA
             SELECT id INTO v_wallet_id 
               FROM credit_wallet 
              WHERE user_id = NEW.user_id 
              LIMIT 1;

              -- WALLET YOKSA OLUŞTUR (BIREYSEL)
              IF v_wallet_id IS NULL THEN
                INSERT INTO credit_wallet (
                    user_id,
                    company_id,
                    active,
                    created_date,
                    total_credits,
                    used_credits,
                    remaining_credits,
                    last_calculated_at
                ) VALUES (
                    NEW.user_id,
                    NULL,
                    1,
                    NOW(),
                    0,
                    0,
                    0,
                    NOW()
                );
                SET v_wallet_id = LAST_INSERT_ID();
              END IF;
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

    -- CASE 2: PAID -> NOT PAID (Revoke Credits)
    ELSEIF NEW.user_order_status_id NOT IN (2, 5, 6)
       AND OLD.user_order_status_id IN (2, 5, 6) THEN

        UPDATE credit_transaction
           SET active = 0
         WHERE user_order_id = NEW.id;

    END IF;

END$$

DELIMITER ;

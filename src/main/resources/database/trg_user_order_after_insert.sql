DROP TRIGGER IF EXISTS trg_user_order_after_insert;
DELIMITER $$

CREATE TRIGGER trg_user_order_after_insert
AFTER INSERT ON user_order
FOR EACH ROW
BEGIN
    DECLARE v_credit INT DEFAULT 0;
    DECLARE v_wallet_id BIGINT DEFAULT NULL;

    -- Yalnızca PAID (2), CAMPAIGN (5) ve MANUAL (6) siparişlerde işlem yapılır
    IF NEW.user_order_status_id IN ('2', '5', '6') THEN

        -- Wallet belirleme mantığı (Kurumsal öncelikli)
        IF NEW.company_id IS NOT NULL THEN
            -- Kurumsal Cüzdan Ara
            SELECT id INTO v_wallet_id
            FROM credit_wallet
            WHERE company_id = NEW.company_id
            LIMIT 1;

            -- Yoksa Kurumsal Cüzdan Aç (user_id NULL olmalı)
            IF v_wallet_id IS NULL THEN
                INSERT INTO credit_wallet (user_id, company_id, created_date, active)
                VALUES (NULL, NEW.company_id, NOW(), 1);
                
                SET v_wallet_id = LAST_INSERT_ID();
            END IF;
        ELSE
            -- Bireysel Cüzdan Ara
            SELECT id INTO v_wallet_id
            FROM credit_wallet
            WHERE user_id = NEW.user_id
            LIMIT 1;

            -- Yoksa Bireysel Cüzdan Aç
            IF v_wallet_id IS NULL THEN
                INSERT INTO credit_wallet (user_id, company_id, created_date, active)
                VALUES (NEW.user_id, NULL, NOW(), 1);

                SET v_wallet_id = LAST_INSERT_ID();
            END IF;
        END IF;

        -- Lisans paketinden kredi miktarını al
        SELECT credit_amount INTO v_credit
        FROM license_package
        WHERE id = NEW.license_package_id;

        -- Kredi işlem kaydı oluştur (PURCHASE = id: 1)
        INSERT INTO credit_transaction (
            active, amount, credit_transaction_type_id,
            user_id, wallet_id, user_order_id, created_date
        )
        VALUES (
            1, v_credit, 1,
            NEW.user_id, v_wallet_id, NEW.id, NOW()
        );

        -- Wallet'ı güncelle (SP, trigger, procedure veya manuel)
        CALL sp_recalculate_wallet(v_wallet_id);

    END IF;

END$$

DELIMITER ;

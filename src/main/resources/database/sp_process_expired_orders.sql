DELIMITER $$
DROP PROCEDURE IF EXISTS `sp_process_expired_orders`$$

CREATE DEFINER=`root`@`%` PROCEDURE `sp_process_expired_orders`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_order_id BIGINT;
    DECLARE v_user_id VARCHAR(36);
    DECLARE v_company_id BIGINT;
    DECLARE v_credit_amount INT;
    DECLARE v_wallet_id BIGINT;
    
    -- Cursor for finding active orders that have expired
    DECLARE cur_expired CURSOR FOR 
        SELECT 
            uo.id, 
            uo.user_id, 
            uo.company_id, 
            lp.credit_amount 
        FROM user_order uo
        INNER JOIN license_package lp ON uo.license_package_id = lp.id
        WHERE uo.active = 1 
          AND uo.expires_at < NOW()
          AND lp.credit_amount > 0;
          
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur_expired;

    read_loop: LOOP
        FETCH cur_expired INTO v_order_id, v_user_id, v_company_id, v_credit_amount;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 1. IDENTIFY WALLET (Company or User)
        SET v_wallet_id = NULL;
        
        IF v_company_id IS NOT NULL THEN
            -- If Company Order, find Company Wallet
            SELECT id INTO v_wallet_id FROM credit_wallet WHERE company_id = v_company_id LIMIT 1;
        ELSE
            -- If User Order, find User Wallet
            SELECT id INTO v_wallet_id FROM credit_wallet WHERE user_id = v_user_id LIMIT 1;
        END IF;

        IF v_wallet_id IS NOT NULL THEN
            -- 2. INSERT TRANSACTION (Type 5: EXPIRED)
            -- Note: balance_after will be fixed by sp_recalculate_wallet, 
            -- but we can put 0 or current for now. sp_recalculate_wallet updates the wallet, 
            -- but does not update transaction history balance_after usually.
            -- However, let's insert the record.
            INSERT INTO credit_transaction (
                active, 
                amount, 
                created_by, 
                created_date, 
                note, 
                credit_transaction_type_id, 
                user_id, 
                user_order_id, 
                wallet_id
            ) VALUES (
                1, 
                v_credit_amount, 
                'sp_process_expired_orders', 
                NOW(), 
                'Package validity expired', 
                5, -- EXPIRED
                v_user_id, 
                v_order_id, 
                v_wallet_id
            );
            
            -- 3. DEACTIVATE ORDER
            UPDATE user_order 
               SET active = 0, 
                   updated_by = 'sp_process_expired_orders', 
                   updated_date = NOW() 
             WHERE id = v_order_id;
            
            -- 4. RECALCULATE WALLET
            CALL sp_recalculate_wallet(v_wallet_id);
            
        END IF;
        
    END LOOP;

    CLOSE cur_expired;
END$$
DELIMITER ;

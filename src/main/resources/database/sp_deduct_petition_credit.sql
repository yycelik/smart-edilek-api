DROP PROCEDURE IF EXISTS `sp_deduct_petition_credit`;
DELIMITER $$

CREATE DEFINER=`root`@`%` PROCEDURE `sp_deduct_petition_credit`(
    IN p_petition_id BIGINT
)
BEGIN
    DECLARE v_user_id VARCHAR(255);
    DECLARE v_company_id BIGINT;
    DECLARE v_wallet_id BIGINT;
    DECLARE v_cost INT;
    DECLARE v_current_balance INT;
    DECLARE v_spend_type_id BIGINT;
    
    -- 1. Get Petition Info
    SELECT user_id, company_id, credit_cost 
    INTO v_user_id, v_company_id, v_cost
    FROM petition 
    WHERE id = p_petition_id;
    
    IF v_cost IS NOT NULL AND v_cost > 0 THEN
    
        -- 2. Find Wallet
        IF v_company_id IS NOT NULL THEN
            -- Company Wallet
            SELECT id, remaining_credits INTO v_wallet_id, v_current_balance
            FROM credit_wallet
            WHERE company_id = v_company_id AND user_id IS NULL LIMIT 1;
        ELSE
            -- User Wallet
            SELECT id, remaining_credits INTO v_wallet_id, v_current_balance
            FROM credit_wallet
            WHERE user_id = v_user_id LIMIT 1;
        END IF;
        
        -- 3. Check Balance and Deduct
        IF v_wallet_id IS NOT NULL AND v_current_balance >= v_cost THEN
            
            -- Get SPEND type ID (assuming code='SPEND')
            SELECT id INTO v_spend_type_id FROM credit_transaction_type WHERE code = 'SPEND' LIMIT 1;
            
            IF v_spend_type_id IS NOT NULL THEN
                INSERT INTO credit_transaction (
                    user_id, 
                    wallet_id, 
                    petition_id, 
                    credit_transaction_type_id, 
                    amount, 
                    active, 
                    created_date
                ) VALUES (
                    v_user_id,
                    v_wallet_id,
                    p_petition_id,
                    v_spend_type_id,
                    v_cost, -- Positive amount as per logic, sp_recalculate subtracts USAGE/SPEND(type 2)
                    1,
                    NOW()
                );
                -- Trigger `trg_credit_transaction_after_insert` will auto-calculate wallet
            END IF;
        END IF;
    END IF;
END$$
DELIMITER ;

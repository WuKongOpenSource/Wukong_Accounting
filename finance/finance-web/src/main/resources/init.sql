INSERT INTO `wk_finance_currency` (`currency_id`, `currency_coding`, `currency_name`, `exchange_rate`, `home_currency`, `create_user_id`, `create_time`, `status`,`account_id`) VALUES ({autoId}, 'RMB', '人民币', '1.00', 1, '671', now(), 1,{accountId});
INSERT INTO `wk_finance_voucher` (`voucher_id`, `voucher_name`, `print_titles`, `is_default`, `sort`,  `create_user_id`, `create_time`, `account_id`) VALUES ({autoId}, '记', '记账凭证', '1', NULL, {userId}, '{createTime}', {accountId});
INSERT INTO `wk_finance_voucher` (`voucher_id`, `voucher_name`, `print_titles`, `is_default`, `sort`,  `create_user_id`, `create_time`, `account_id`) VALUES ({autoId}, '收', '记款凭证', '0', NULL, {userId}, '{createTime}', {accountId});
INSERT INTO `wk_finance_voucher` (`voucher_id`, `voucher_name`, `print_titles`, `is_default`, `sort`,  `create_user_id`, `create_time`, `account_id`) VALUES ({autoId}, '借', '收款凭证', '0', NULL,  {userId}, '{createTime}', {accountId});
INSERT INTO `wk_finance_voucher` (`voucher_id`, `voucher_name`, `print_titles`, `is_default`, `sort`,  `create_user_id`, `create_time`, `account_id`) VALUES ({autoId}, '付', '付款凭证', '0', NULL, {userId}, '{createTime}', {accountId});
/**
 * radp-cms
 * @Title: AccountServiceImpl.java 
 * @Package com.xzjie.gypt.system.service.impl
 * @Description: TODO(添加描述) 
 * @Copyright: Copyright (c) 2016
 * @Company:
 * @author 作者 E-mail: 513961835@qq.com
 * @date 2016年6月18日
 */
package com.xzjie.gypt.system.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xzjie.gypt.common.dao.BaseMapper;
import com.xzjie.gypt.common.page.PageEntity;
import com.xzjie.gypt.common.utils.RegexUtils;
import com.xzjie.gypt.common.utils.constants.ConstantsUtils;
import com.xzjie.gypt.core.service.AbstractBaseService;
import com.xzjie.gypt.system.dao.AccountMapper;
import com.xzjie.gypt.system.entity.AccountEntity;
import com.xzjie.gypt.system.model.Account;
import com.xzjie.gypt.system.model.AccountRole;
import com.xzjie.gypt.system.service.AccountRoleService;
import com.xzjie.gypt.system.service.AccountService;

/**
 * @className AccountServiceImpl.java
 * @description TODO(添加描述)
 * @author xzjie
 * @create 2016年6月18日 下午5:30:06
 * @version V0.0.1
 */
@Service("accountService")
public class AccountServiceImpl extends AbstractBaseService<Account, Long> implements AccountService {

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private PasswordHelper passwordHelper;

	@Autowired
	private AccountRoleService accountRoleService;
	
	@Override
	protected BaseMapper<Account, Long> getMapper() {
		return accountMapper;
	}

	@Override
	public Account getAccountLogin(String username, String stype) {
		Account record = new Account();
		record.setStype(stype);

		// 用户名查询
		if (RegexUtils.checkUserName(username)) {
			record.setName(username);
		} else if (RegexUtils.checkMobile(username)) { // 手机号查询
			record.setPhone(username);
		} else if (RegexUtils.checkEmail(username)) { // 邮箱查询
			record.seteMail(username);
		}

		return accountMapper.getEntity(record);
	}

	@Override
	public void save(AccountEntity parameter) {
		Account model = parameter.getModel();
		AccountRole arModel = new AccountRole();

		this.save(model);

		arModel.setUserId(model.getUserId());
		arModel.setRoleId(parameter.getRoleId());

		accountRoleService.save(arModel);
	}

	@Override
	public boolean save(Account record) {

		record.setCreateDate(new Date());
		record.setLocked(Integer.parseInt(ConstantsUtils.ACCOUNT_NOT_LOCKED));
		record.setState(1); // 状态 1正常，0失败
		passwordHelper.encryptPassword(record);
		int row = accountMapper.insert(record);
		return row > 0 ? true : false;
	}

	@Override
	public void update(AccountEntity parameter) {
		AccountRole accountRole = new AccountRole();

		Account model = parameter.getModel();

		accountRole.setUserId(model.getUserId());
		accountRole.setRoleId(parameter.getRoleId());

		accountRoleService.updateAccountRole(accountRole);
		this.update(model);
	}

	@Override
	public boolean update(Account record) {
		int row = accountMapper.update(record);
		return row > 0 ? true : false;
	}

	@Override
	public void batchUpdate(List<Account> records) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean delete(Long id) {
		Account record = new Account();
		record.setUserId(id);
		record.setState(0); // 状态 1正常，0失败
		return this.update(record);
	}

	@Override
	public void batchDeleteById(List<Long> records) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Account> getListPage(PageEntity<Account> record) {
		return accountMapper.findListPage(record);
	}

	@Override
	public AccountEntity getAccount(Long userId) {
		AccountEntity entity = new AccountEntity();

		Account account = this.get(userId);
		AccountRole accountRole = accountRoleService.getAccountRoleByUserId(userId);

		entity.setModel(account);
		if (accountRole != null) {
			entity.setRoleId(accountRole.getRoleId());
		}
		return entity;
	}

	@Override
	public boolean changePassword(Long userId, String password, String newPass) throws Exception {
		Account changeAccount = new Account();
		Account record = new Account();

		record.setUserId(userId);

		Account account = accountMapper.getEntity(record);

		String pass = account.getPassword();

		account.setPassword(password);// 明文 旧密码

		if (!passwordHelper.verifyPassword(account, pass)) {
			throw new Exception("旧密码错误");
		}

		account.setPassword(newPass);// 明文 新密码
		passwordHelper.encryptPassword(account);

		changeAccount.setUserId(userId);
		changeAccount.setPassword(account.getPassword());
		changeAccount.setSalt(account.getSalt());

		return this.update(changeAccount);
	}

	@Override
	public boolean resetPassword(Long userId) {
		Account record = new Account();
		record.setUserId(userId);

		Account account = accountMapper.getEntity(record);

		account.setPassword(ConstantsUtils.DEFAULT_PASSWORD);

		passwordHelper.encryptPassword(account);

		record.setPassword(account.getPassword());
		record.setSalt(account.getSalt());

		return this.update(record);
	}

	@Deprecated
	@Override
	public boolean isNameExist(String name) {
		return this.isExist(name, 1,null);
	}

	@Deprecated
	@Override
	public boolean isPhoneExist(String phone) {
		return this.isExist(phone, 2,null);
	}

	@Deprecated
	@Override
	public boolean isEmailExist(String eMail) {
		return this.isExist(eMail, 3,null);
	}

	/**
	 * 
	 * @param value
	 * @param type
	 *            1 用户名，2 手机号，3 邮箱
	 * @return
	 */
	private boolean isExist(String value, int type,Long userId) {
		Account account = new Account();

		if (1 == type) {
			account.setName(value);
		} else if (2 == type) {
			account.setPhone(value);
		} else {
			account.seteMail(value);
		}
		
		if(userId!=null){
			account.setUserId(userId);
		}

		return accountMapper.exist(account) > 0;
	}

	@Override
	public long getIdMaxValue() {
		return accountMapper.getIdMaxValue();
	}

	@Override
	public boolean isNameExist(String name, Long userId) {
		return this.isExist(name, 1,userId);
	}

	@Override
	public boolean isPhoneExist(String phone, Long userId) {
		return this.isExist(phone, 2,userId);
	}

	@Override
	public boolean isEmailExist(String eMail, Long userId) {
		return this.isExist(eMail, 3,userId);
	}



}

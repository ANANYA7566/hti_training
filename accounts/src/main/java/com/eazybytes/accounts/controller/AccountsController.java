
/**
 * 
 */
package com.eazybytes.accounts.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.accounts.AccountServiceConfig;
import com.eazybytes.accounts.feignclients.CardsFeignClient;
import com.eazybytes.accounts.feignclients.LoansFeignClient;
import com.eazybytes.accounts.model.Accounts;
import com.eazybytes.accounts.model.Cards;
import com.eazybytes.accounts.model.Customer;
import com.eazybytes.accounts.model.CustomerDetails;
import com.eazybytes.accounts.model.Loans;
import com.eazybytes.accounts.model.Properties;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;



/**
 * @author Eazy Bytes
 *
 */

@RestController

public class AccountsController {
	
	@Autowired
	private AccountsRepository accountsRepository;
	
	@Autowired
	private CardsFeignClient cardsFeignClient;
	@Autowired
	private AccountServiceConfig accountServiceConfig;
	@Autowired
	private LoansFeignClient loansFeignClient;
	
//	@GetMapping("/accounts/serviceConfig")
//	public AccountServiceConfig getPropertyDetails() {
//		return accountServiceConfig;
//	}

	@GetMapping("/accounts/serviceConfig")
	public Properties getPropertyDetails() {
		
		Properties property = 
				new Properties
				(accountServiceConfig.getMsg(),accountServiceConfig.getBuildVersion(),accountServiceConfig.getMailDetails(), accountServiceConfig.getActiveBranches());
		return property;
	}

	@PostMapping("/myAccount")
	public Accounts getAccountDetails(@RequestBody Customer customer) {
		
		accountsRepository.findAll()
			.forEach(c -> System.out.println(c));

		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		
		if (accounts != null) {
			return accounts;
		} else {
			return null;
		}

	}

	@PostMapping("/api/GetCustomerDetails")
	@CircuitBreaker(name="customerDetailsInAccountService",fallbackMethod="myCustomerDetailsFallback")
	@Retry(name="MyCustomerDetails")
	public CustomerDetails getCustomerDetails(@RequestBody Customer customer) {
		//get the customer account details 
		CustomerDetails customerDetails = new CustomerDetails();
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());

	    List<Cards> cards = cardsFeignClient.getCardsDetails(customer);
		List<Loans> loans = loansFeignClient.getLoansDetails(customer);
		customerDetails.setAccount(accounts);
		customerDetails.setCards(cards);
		customerDetails.setLoans(loans);
		return customerDetails;
	}
	
	private CustomerDetails myCustomerDetailsFallback(Customer customer, Throwable t
			) {
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		CustomerDetails customerDetails = new CustomerDetails();
//	    List<Cards> cards = cardsFeignClient.getCardsDetails(customer);
		List<Loans> loans = loansFeignClient.getLoansDetails(customer);
		customerDetails.setAccount(accounts);
//		customerDetails.setCards(cards);
		customerDetails.setLoans(loans);
		return customerDetails;
		
	}
	
	
	
}

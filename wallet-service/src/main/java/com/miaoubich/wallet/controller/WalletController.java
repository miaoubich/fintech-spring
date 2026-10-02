package com.miaoubich.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.wallet.dto.AssetBalanceResponse;
import com.miaoubich.wallet.dto.WalletResponse;
import com.miaoubich.wallet.dto.WalletSummaryResponse;
import com.miaoubich.wallet.service.WalletService;

@RestController
@RequestMapping("/wallet")
public class WalletController {

	private final WalletService walletService;
	
	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<WalletResponse> getWallet(@PathVariable String userId){
		return ResponseEntity.ok(walletService.getWallet(userId));
	}
	
	@GetMapping("/{userId}/{symbol}")
	public ResponseEntity<AssetBalanceResponse> getAssetBalance(@PathVariable String userId, @PathVariable String symbol) {
		return 
				ResponseEntity.ok(walletService.getAssetBalance(userId, symbol));
	}
	
	@GetMapping("/{userId}/summary")
	public ResponseEntity<WalletSummaryResponse> getWalletSummary(@PathVariable String userId){
		return
				ResponseEntity.ok(walletService.getWalletSummary(userId));
	}
}

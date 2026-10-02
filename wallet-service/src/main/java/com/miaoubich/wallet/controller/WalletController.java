package com.miaoubich.wallet.controller;

import com.miaoubich.wallet.dto.AssetBalanceResponse;
import com.miaoubich.wallet.dto.WalletResponse;
import com.miaoubich.wallet.dto.WalletSummaryResponse;
import com.miaoubich.wallet.service.WalletService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;

@Controller("/wallet")
public class WalletController {

	private final WalletService walletService;
	
	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}
	
	@Get("/{userId}")
	public HttpResponse<WalletResponse> getWallet(@PathVariable String userId){
		return HttpResponse.ok(walletService.getWallet(userId));
	}
	
	@Get("/{userId}/{symbol}")
	public HttpResponse<AssetBalanceResponse> getAssetBalance(@PathVariable String userId, @PathVariable String symbol) {
		return 
				HttpResponse.ok(walletService.getAssetBalance(userId, symbol));
	}
	
	@Get("/{userId}/summary")
	public HttpResponse<WalletSummaryResponse> getWalletSummary(@PathVariable String userId){
		return
				HttpResponse.ok(walletService.getWalletSummary(userId));
	}
}

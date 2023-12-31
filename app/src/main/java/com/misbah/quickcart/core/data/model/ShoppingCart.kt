package com.misbah.quickcart.core.data.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * @author: Mohammad Misbah
 * @since: 17-Dec-2023
 * @sample: Technology Assessment for Sr. Android Role
 * Email Id: mohammadmisbahazmi@gmail.com
 * GitHub: https://github.com/misbahazmi
 * Expertise: Android||Java/Kotlin||Flutter
 */
class ShoppingCart(private val taxRate: Double = 0.05) { // Tax rate set to 5% by default
    private val cartItems: MutableMap<Product, Int> = mutableMapOf()

    fun addToCart(product: Product, quantity: Int) {
        if (cartItems.containsKey(product)) {
            val updatedQuantity = cartItems[product]!! + quantity
            cartItems[product] = updatedQuantity
        } else {
            cartItems[product] = quantity
        }
    }

    fun removeFromCart(product: Product, quantity: Int) {
        if (cartItems.containsKey(product)) {
            val updatedQuantity = cartItems[product]!! - quantity
            if (updatedQuantity <= 0) {
                cartItems.remove(product)
            } else {
                cartItems[product] = updatedQuantity
            }
        }
    }

    fun getCartItems(): Map<Product, Int> {
        return cartItems.toMap()
    }

    fun getSubtotal(): Double {
        var subtotal = 0.0
        for ((product, quantity) in cartItems) {
            subtotal += product.price * quantity
        }
        return subtotal
    }

    fun getTaxAmount(): Double {
        return getSubtotal() * taxRate
    }

    fun getTotalPrice(): Double {
        return getSubtotal() + getTaxAmount()
    }

    fun getTaxPriceFormatted(): String {
        return DecimalFormat( "AED #0.00" ,  DecimalFormatSymbols( Locale.ENGLISH)).format( getTaxAmount() )
    }

    fun getSubTotalPriceFormatted(): String {
        return DecimalFormat( "AED #0.00" ,  DecimalFormatSymbols( Locale.ENGLISH)).format( getSubtotal() )
    }

    fun getTotalPriceFormatted(): String {
        return DecimalFormat( "AED #0.00" ,  DecimalFormatSymbols( Locale.ENGLISH)).format( getTotalPrice() )
    }

    fun clearCart() {
        cartItems.clear()
    }
}

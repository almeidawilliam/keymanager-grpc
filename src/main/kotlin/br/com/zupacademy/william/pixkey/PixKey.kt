package br.com.zupacademy.william.pixkey

import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
/* TODO
 *      - nao guardar dados do cliente, somente sua referencia
 */
class PixKey(

    @Enumerated(EnumType.STRING)
    val pixKeyType: KeyType,
    val pixKeyValue: String,
    val idCustomer: String,
    @Enumerated(EnumType.STRING)
    val accountType: AccountType,
    val agency: String,
    val accountNumber: String,
    val ispb: String
) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
}

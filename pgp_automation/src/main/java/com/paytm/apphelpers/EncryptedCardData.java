package com.paytm.apphelpers;


import com.paytm.dto.PaymentDTO;

public class EncryptedCardData {
    
    public static String getCardData(String cardNumber){
        if (cardNumber.equals(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER)){
            return "b/5ritAzH9+XkUKPOjyova7PMtqWcfHwIHWP8jpVaCYhmE963RiNHecFkhSLlTGBfk+PMR+CsstGgTZ7f0Dzr+LTav2nDD/Vs9S5siekAC1vsLJYKyuUYs1bZssXeNuimwoVdFaskop2ZWo6rKK3F/b9ihipK8V6JBI1dgswdXrZIvd5J0XwqW2Ritx+9x0MFewbBGrmGSWejYGo0uGe2TGDakzg6veT/9He7bi+EvahkHkyTj6Ulkc2AHpKxnfkoDmLnOHm24LFRSx919ISHSlAOisfCp06CZRWMaBg/hzuCPNoc1UeX/K5lkcBfNAUzfxlwz/DesCNCQ8ODLCLzw==";
        }
        else if (cardNumber.equals(PaymentDTO.MASTER_CREDIT_CARD)) {
            return "WL6KQJKVZELR4HjegVpnC95gSBaEzIS2uDqkK019ObODCAUwi7NUVfC9vsOCFwhvmhm5oMzALutXYTMmlVswabphpFRjiddS9SI0jiDIWDPM4z46Mhi5u9Io3g6IyBGdl5FKc8gEebRMyelfc5VBexgaKhW+2+eUDiTJ4BUkd7uVZScpdcPGTJnx1XtDv/TQFwUxCKJoqnwyfrHIBzEqi/1aCBCnN6m/v2oUmXqTfwxbGRwBUumjcei2VHhfkUHjK57OA+2fK4ojDrPzQ+l7bsxJNnbJ2tydoI03BeniHDpLBUdQeNQiDBukhpQUCETfMwIKyjH6hihYVjmUHe/fOw==";
        }
        else if (cardNumber.equals(PaymentDTO.DEBIT_CARD_NUMBER)){
            return "lDH9Ike60LK3wsfgVNCCcYcvHCwTAcRWw7wAGQhEmcI2qr8qUfiLtByGqGOLq4PmZHg0kG35b3JZTPixXgv6uefJ7WGz7mz7KyYV9dmATVxTeoJOuJgk2tHPRHV6EMZ1drAcJVFJWtA0Rr1b6A+6V38BShHtNRTkQcoBnNHnyPv+q90ZwR6C7jL8JIARWEdl2rRRWzg+r9wMs5NO6spDbxEMApcz1wD3IlfwAM9i1ftc95fFU2lZtb1gwMOLNsBh2MGRTFhg/OeGpbemcUGCuY8LGMjoxSoLGaP+IQn/rKnAx54FxnniCf7eIGxRvZS3u2GpTe9do4UPy6QdL3AgkA==";
        }
        else if (cardNumber.equals(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN)) {
            return "YRpTve5YzeYZWKxgIuIjCMR+0D2QxBC7ycV6TxKKkeJhnmqIvQuuqEAOE0wi/NShtFCCk4JOYOZk+NzL3z7EMw6mF1N3enDl+IYvEYSslca9UKoOe56Av5O8UpbnKD2fiB8HVJ37Fpzp197xyA3M69x1Nx86/dbIRBWzzQFOXKk9OfzAdud/W+ycxRQeQLA2yzfHUoOD/c1NPuwN81vWzASyHWWHOW0Hrs0rMUvqou1EH135NiHRqrBg9SfdUrdiDmzTV5Ee9T1yjwLlpd2ZOgzNX71AkZ9FKN0hBs/vMBq3WPIRkzqTzErtfmWauzYs+8G4UVAmHTQMkd03+MzHhQ==";
        } else if (cardNumber.equals(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER)) {
            return "Cpn8XGbLwOABImUnRcULRrZgNPE+X+xFUKEYjdplWBAj/a1Y7RoC8NXMkZydBlgYTP2A4sNHj/NoKtXiO2AJpCQ43MLBIFLguCfRGkJoaI3f7hR5/UQdYcDngTscQiXzbnD4YySPZkxmhOHLBIZi/FQ4CpmRBYObLDnTFGS3eAKr5ZiZOKgLHxk/UDEwJ3eH7IcNlqMDM/KNADY1eqN+GKrFPrm9Feh0lbwA1CkjH+kKGCAiQtwo2KsTj4UNkl3Y6EmcLNlVOTaf2pAgkRwEiSSXDryyvXSWs9x9cWk27fR+Ik7rm17ojx3DZnjifk9Ms0KWmFnZuRWhKhdEtghi2g==";
        }
        else if (cardNumber.equals(PaymentDTO.ICICI_CORPORATE_DEBIT_CARD_NUMBER)){
            return "PFBYFYYjAHZTk3MowUM/p/aMaWkCkrI4SSPk2rSZerTrLFfPqrgnedNM5l13rbYC0tlnzkFvN2Y07xttlG70ir3o4RAH5XfnR9y+hsH3edpnRqjkmVJ+8va/t1c1OoRq0GTio4rdXqeWGBjB/qnP661fzUbU0dssM/kKT9zcXirML8/EFrRqswYuGQxAFgxIWjXlMkJvoewbamOZSzMUUvoAnE2tXns8b1JDoFUbZK83SQWhoGBnywM+fhdeR8g/ZBg6dhKg2CY3zX049otyk5Me6SqFTsOWhneDUXNV1T0RG5EiKr+sm/m1PceRhPfpymcsddtpKPzxYaoPi0blaw==";
        }
        else if (cardNumber.equals(PaymentDTO.ICICI_DEBIT_CARD_NUMBER)){
            return "izW1SA72hSv3DS+KG5vmXiWJy80YQti3H9xRZZy+OAyaOhCkvmpJU2wrnudX1rLSSRVN9tajkl6nhnHfHwJ2JZvJWYWnlmudaV6SUIQmZWmgeenOr6eyZxh0y6eyYJ/CryJbGYlILZi1VosJkLTF9b4hLXs8+ZySBHOoLg/FSgD+uZ4wAqf3DgNPDusB5piUr/h+DVSs0pZh7kvt8yKA4ptaSYbkL0A8o/RRPkCiSO6XHw1RVvAy6jxzdUWnV+IT9dGYG3jcCZEIDkVtht5JexgDnmCJCcPXQe4DBIQ4pbBIiU67B9suVgTHHXoa0Ri1Wi+SBVxm6DK3Yx5sG5ufLA==";
        }

        else if (cardNumber.equals(PaymentDTO.AMEX_CARD_NUMBER)){
            return "b0rKCpYPvLqQgIQS5b1tWz5IE6Y8yLnL1j9769ksVD3RHDEkxRXdHSXIBlV7/rTATs4iLA7z1NAfiQwVSSpfDDOnVjtKfk5ulLy10P9XJS/wv5Xd7sjnmhyObRXr0V0M9ZfROj/+JpxJP2i0KIgSUzA15yAXlIuzCgO5lDmyXY0joJef/2IEUhCkoP6WrcscjH6VyqnlIt7HeqqtIYOfcX6A67JG4qjT468qNB7Fb/pVg4QIP/IGuBpMMpieLzRCTgLXtMIPIWKwog9jB56iWZ2FAl1ySjJ8t8YCQQBc/a3ZMge8dJkQWa2ZkckZaOIp+Y163PnYoO+HUCzfELzi+A==";
        }
        else if (cardNumber.equals(PaymentDTO.VISA_COFT_CARD_NUMBER)){
            return "L7xhY+2Egwt2IEc/N2FeuiEEkVTdPdrmpemHD8Vh36B0VCm+e6aGuxWmNH0k4sU2+1OCzeR4cPcxu+wdLcJW8L5LmWFLLCEUDiWPqhhJ31AQ8ZTxvodJxhg+V2iu3mMhFNe2Aft6O3kBNjD0u9VThI5eqIXwiauer0s9UhrmUEEQW06o5fA3oZ6/WqoNkq1ZK4pjn7Iw6AfyFuOVla0Wj/l5c7VjO5QGkmxFCyVbkOxMYnYQi80omeOFrT1cTC2/UsgejhAc+Ucy6S5s2pIQsQTNlJfDRQfdmyVFiryVWVOPRhCYgdjzDTy0GbHvBn7autpNKC3Hm3wEsfW9IbDDFg==";
        }
        else if (cardNumber.equals(PaymentDTO.RUPAY_CARD_NUMBER)){
            return "lLUMfFW3r/JliqlSfDjMh2Kxe8A6u6tmoCrbkDfOc7EaEqSzfFGN5Q2rul1mjgTBjEogScZWcNNY9JFdlIR3JlbEFTo3GXHfvAWtpODZuF83FRvGn7HeRfQBhtifIYfGK1iFcA5Ymwgn+0g1kCfs2WJVYMUlUVuUi68pB1e8c8odBCI+eraaQzSjx/c6UQnukVmgylv4rWfBQEdSJXz60a+zQRVH51f7kPGnCdHGDvylkTsI7rDyi+qPxmGZfODeh6q4/ShqVLc9am4Dddhgb/c1oAooEzBJiueNYBhuTJT4MoB5Q7OtAR7ScycVLsOayA1gxzFwwG4whtaKxYbFVA==";
        }
        else if (cardNumber.equals(PaymentDTO.DINERS_CARD_NUMBER)){
            return "JmfqeOt7I4nkppgzAWsvRP3DtTdiwMvgBHW3pXZEBWA64MgBYTkcK3rdw7ya3kLg/nAEya7y0q8qinfX5MRbGCZgayyQYbPMAhZ/pwzCb8lbGoVJJ8FCC26zryvp8WPIjG0rhXpsx/bLDBjkWRqdLchFobgsrqPmdR3O8xjWZU7QxxCh10UAd4RdyPVgaDy3goUc9hLDtd7BGP/VoeBzUY6toBblvU8yuIGGQnW7EDfqF5yE2c9NNGBp9zq9C8z+D6zKD/9ITx5/H06ZHWRmEEfjq2Kv2e1e3CMPySRPj8VhbJm8Bf0tJHZVXeFncC6SRdOwOpCYfFp75IpNG9WDMQ==";
        }
        else if (cardNumber.equals(PaymentDTO.INVALID_CARD)){
            return "D9oS3Tb436GRTmuvzin58tfMYvmfReqOC+M0pCbTM4wSzNuJdrkj3WEV0VWb72h+WcR0kfdSAu0toFzqqQ1Z+MtVT6F4dgbuyh5kvgzohBbEOnyv9o4IfoPKkiGlZ5LZEiZO19F7/YVuQn2vCvWuzB3k2+fLyvRmuCalTb8iZhTEDxSXF6XkrgLd9GOGVzsNwRuPwcTQYVqpRvmehL+kb/vmThZf/ZGURH9GOu4RDY5cb+JYe2gB99V5TKWUWmYiKXjbwVVyYrG0oWk1ggixoIvFSWxDEoEF8Tolrvwl0twrOEugRJk0rRQS22RTcf4E1kFtsGK21rU0PJlux0poiw==";
        }
        else if (cardNumber.equals(PaymentDTO.SBI_DEBIT_CARD)){
            return "SXg4zgA0EFYHaGevi6SUsCs4BwmObbePLfrHZWxe0NN3J3u+Nq2JHMHsnZ8FBdNKe+04wFHZJEgmpWMw978doMyyC/NZXtoUTDqF9TE9Wh2TnQxxO1eL6s0sWS5SUpZjYVnXC65xtuRrCoDQwE1tsvDidj5X8EGZlM/GQkw0sBFAs/QVTjEoXB95ey2El/kJX9o9FIy+kzOnOCa2rfEfK7nMG97+BTSYnMxRFeu9E8lf/6OsAkfxQSj8yv955S4EWdvKYv+EjrirFdhgIIfEhc9sV/bkX0xpY3CC+23j6/nXqtrLTXQGmQj+X0Lf/EUfo+99DXsNwUUChJNjI+ToeQ==";
        }
        else if (cardNumber.equals(PaymentDTO.PNB_DEBIT_CARD)){
            return "STyRB2sRRXlvuwT7b2ZrZHAfgyU7FMLMRGUWHqVOGwAc59wSgusA6xgpQCc6b4FnNGSjF8RCKtSEPgqxDNIblzQ6oWTu1IElDFFLyCq/A/s/XPu6pSZ1KAAWiXLBZ8h+9nh2Q2SaVF46MY2tpHpMIjJJG91X7jNFPzG8s0ING4Qjc8zFYx/Kx5P37gazf+5MTlAeDTBjViINzUc80HgE277PDIoamNO3V7OgrR8QLxmGz4zghe8qeLKtr5Tdo85lIqmv+ZSPmQQpPEn3IIQ+dY1fW6vLIya4E3hmrTPFyF1IwvWSmbQT4L013EXQfRkvXXF8aa6yKThbMGsWJwqrrw==";
        }
        else if (cardNumber.equals(PaymentDTO.INTERNATIONAL_CARD)){
            return "hvkv9aN1OUz616+tKbWGgkMyZVoHHTT97Yrx0yJZgvCd31CfVnhZiGYKiK34BwjoYfo7rMKNr8IQKYAGIz6/q2yRqpgWrBGkM9SiA6o3Ew6wVSgzdHkZQxVnBd1VuksTRprp+R0TMHaWyPInGxk4R/QtTRiloKL4Msz+pFoHZO9yPp1p8+75j2HCRoSlALxWkLiH768hH7hZVo9PLFZ3lMch11YcKLrrYvaa7Y2wy75jxpqLDSXW/+X66ptk+/F6PtGhKwCkOHJwml3SK5rLjEeRJEBpqG8SMH9rLZQq2+LLXjyi+dH+mfiZMbElaiDZALtBrE8cE9TPZ9PelcdqYQ==";
        }
        else if (cardNumber.equals(PaymentDTO.INTERNATIONAL_CARD_1)){
            return "bDDKvZQdZvQRmCnsBT5z9vZf7I8wWXQP+1HVDcbhiVmdyM8/qTbAdRmhFiV9SMywYcqXYfssM4Y6bpZmaGlB/fafZemdGtB+nhZ8i3IHGsbPf+2R64jUNns/CfX7Ab7V92sRdwyF3YLiaF4JKD+CvethxIcmJ9SsH2rCVxSXY+1qXneKA32y+IuRfjx5ItWYPHXRRgTB8kA7wKJnS0od1ULX+f0jTLmyzFTc7aus9CkvnAgNCKZt7dn5leXIUPuAfWQuPN2nnvhI3f2sfd6o2GUdva698WE1m3RQ8qd23ZGwTqWK2eh9fzf/BcvQdVCC/tcMnM2bkgR9OLtijv6Vnw==";
        }
        else if (cardNumber.equals(PaymentDTO.DINERS_PRIVILEGE_CONSUMER_CARD)){
            return "aF8+8EvmqZgu0Zo6DAB1BfCsS5SseqPbPK0klnVwKaG0jt5I6u5mX3l9ainiO1FTO1ly5f7F5L3TlrfpqOSNWZLQDvpnqpRgUMMawaTQwkfv3nNHyS3feH3zJAsCmyyWM9pmLsdM5GEJdNesb7NLIkTfUVlQSezjymsTgmELywWwBEREG7IQv8oU5fema9skGAL/T1bE/QqTLmVUigua/u4eL/Wt6z/y8xPwfLIyvlBL9Xvs9ChdLqNByuPLzIMTfnkzwUwdzKZD0qJCrAiZrmg+kuMjGIlCEXWAqIGxTqZhAOyuT8sgOgV5sEsKxBtTvD4vMjaSIN0wxvFeS/78Vw==";
        }
        else if (cardNumber.equals(PaymentDTO.DINERS_BLACK_PREMIUM_HDFC_CARD)){
            return "i1KgMU3ZgxZtpNULR3zsMNZXDxZbkhaerpHCg6rEteuDNNhci+opww9gR+wXyV7LBwZAOsSfOtFePpbB8X+iXh+gMz63jT/ZVNFS0d+b6l/TQMYlvb+KredvN9hBtgnQ5KrGuf/eJZlxLIgVpQV7hgD5ZYWz2aYQNfYempciDX1wCjY24CsbF1filaNVpP2v2mtYo4wEK6xc4kBQ0GPcTyoLd2KiYdu2nh/DOfbPO9Rz3C4VvWadzXveTJl1gSe+DHHr57QCXZWqHohn5+Yf0GnXHbf6flNLtQEbN5B2PdM6W9GqmFz7lgaMPRc78jiiO7YQD+HZl1QOG/F26nNpdQ==";
        }
        else if (cardNumber.equals(PaymentDTO.PLATINUM_TIMES_SIGNATURE_HDFC_CARD)){
            return "ATvFZQuWWwkTZrqdJT2ygHejgsvbbuPSycmug3wO/NTiKjvhFK4de2TcWDC0NM/LpyczeBd3dGB6CTRS5ECp6o6sS5faWRKg5evGoR1/ENBUngtxJXlfuCiK27nK9rFsw3RxaXz86otxFbCasdou11xVskQSYMLLMZey/7HBWByY/rASRQRqg0p6VuiExcby8xdHhytFhXDTgxabbZkiZBnOxCDmGh55mOwGdjO7+LM+9CLUFvbw3L2SHcqLTiOIaoUlEQqfnlq0Quxtwc+n4MMTChepPQCCwFK3TyDiWAOVE/wBSfCMt6DhBy2t2feUtqGU7KWT0ZCTCMLoOoNyWg==";
        }
        else if (cardNumber.equals(PaymentDTO.REGALIA_SIGNATURE_ICICI_CARD)){
            return "Kgy/kEJ2D87Fh04BJxGP7JK4LOmkiigJqx6pJ26oghV/J0GvUWyy1Kvqq5dcYvmep6FBj+/oJ1iB6TOT1EFqIi7TQGiZHRaxl1YtfuZZUocrBevRlij2m1HCyNIVsW+X3LoiMwYMP1RwJQBzb12ae3OVgbn8sfYjpdu1lD9f2zNJoyLqEEkhWfeWZV0D/Ijgtrmx+INhEmkM8iuGzDDixFjuL+yirjiYn/O8lDhyszR3qg00Ox5fn8DThbXey7SDvO2bE0fkX0v4DodheTz3qjgzFozBzkOY7kDzOdanhxSBHPnu9+bwfuNis9Dy6XQJzx1Wus0BCEjQCXeB6tgvRw==";
        }
        else if (cardNumber.equals(PaymentDTO.EDEN_PREMIUM_ICICI_CARD)){
            return "HINdwqYoQ55w9HQ/OP6Um66MgoAunREh1cSYn8BMqd3FFpeWFiahOvtzFKVdx2jwCH1IdjeCrZdB1rGMfXsmSfdbJqhXypVyTJZxDOJKo3MjwRYmWiHxGt/xB7SYHa9i1hDhYxrFqrt2k53VQoeDJRKklA3jqWzrHqHmNYzC+fHV7d7lNxVe4olYvB2xYm99VEVA2P7tUno4fUE01G8yQBWoLxo3rTlpa7+kok5pJEa8Ao44/8ElxSpYl4abxT2fdh8LYYjGij6O2M7ta4m5X7eUIET7BbnSW7TaZrIvTp+Tkz/4eSq4wNw5yKQtRaiAhIwqYGxAw+qs3pn8Jfe/+A==";
        }
        else if (cardNumber.equals(PaymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD)){
            return "Ce8e8RdQyvyyUfDiba/7ncPXHh7UYx4WpwZtGUgu9COS2QbSi72r1x8X9opLDTXhjHRgevg1Z1kuCNsTU8FpIeNeGeKev7AnMvcnxEmZ/Gf3WJWEBfSAcZW5NXthKclyYMskNFA/cqZt+kawEWkEum/6+OkWDrZsaQOeZqQ5wJUAB1Lfd038bqz6IrLztBSHmik8BncYyzdb4JtfrZ+5aMkj+6LwsQOZu5gK0XXTHP91YXzsE5tG8zZ/LfSnYahZJtbOOiQPB2ghRLaIdsmKIpTfWaol5jQqDIOAKENPKk3Gd8tuHUwz5rb9BSQiApk2LGrbpey7cZv2WV3A52GXDg==";
        }
        else if (cardNumber.equals("4854980000514895")){
            return "MmjbAJSR8PngkXQC59PjYyM0MYv6zb2szq2OATTaDI0vY/Hpf+5r9gtNkocAby6lm6lX+3MBiU9DVHWq17TQNqk9hZwxhLqEzXNdraP/k/iWm095RfQM1d+RRXpo+Zh+UHLAj1aPj6b3jEODF8Y42CNMg5H+eTlwNdwCNH00ImI/FfQ9FbS6mbr9EDEO4OtX1F1dUJZplNyRDOaIZSD/1ygnU/H41UR+xYpPNroUJ5as1unSchVi54BmJn84toFQa0d8muYyP1LTZJkE0ffDbQwG8Pg35yincmnG7SawlLKpys8276YKEaQ5STzyw4UzzDmD0xYPiz3ROJvTqO6O0w==";
        }
        else if (cardNumber.equals("4854980604790867")){
            return "SK/ekmzm/IqGI3K6jpwJRAm9ZKsAq3T9o9ZwfciDXfMO02J2L3yWmM2YLydqqNHinqr1U+lrOxdCs+ebpz0tJYxY53Jjv36dv4rt5jYt3hS6r3Bg6fSGkOEHoNiCL0/n9MprP54WSZ3/tRHxEcoSdM7SF7T8VEJnCFuilD3/eMbH4IG/MQZeiM80cE+AhCRWAyOMWytNHTAeqI30il6q8NJv6RaWa3Lv/BjfwF6mU9JF1SAcAR1xd8PNy5u+bbPQEIRyLKElSbnzdvjkNPr86Cb/oC34TiN/pQNOYNq3gQSBbzYzIRutW2ykGCqtWIsLWQXOKyEW0BYsQ40AhKyEOQ==";
        }
        else if (cardNumber.equals("5459649100225134")){
            return "LCHiMyVkvogZTh+HbNMICH1GCUI+NI1APKmrR63o0g6nXCXFsL9BIy6lcspkOTPntwwsmouuQBHfljr3+iBzfmAPuG5O7wGXT6mhtVixIrMRcFqhy0D1vRxHGhcEJZPS3+cm79Tn5eGRGItaIzN8SyEX4dh+ttYK5enscZxqTuJfCYoAbplIyN4cDnrCQnN3/JC+SjmW7E7cJsBp3KmcLddw6hKliwmQdTB3tRH86V9GJlx1QNsZA3HNE5EEv4cjU0uMjBekxQZ33Miy/w4SJGRourLfCraSxKU3X3DKrHLyIisb3b3EiOH3hJ5SP/Ix5dW5CIa3VknEbbfjueAymw==";
        }
        else if (cardNumber.equals("4893771000362085")){
            return "ISd/Q+toqaO78CiWLR2FxsXba0kLsewSl6EecaGLD7fJTsxZdWh3HSjQ/RBQB46Pb1QyCxHtz/zQ40b9uhbOBvJPe5SHF4PTVa2+YQfB5B+MxvAGTp8b63ULvhkA4i1jMYSqZ5Lu1LyCXpb2QdXjujCcOcEjY2JAO5fPts1lCelLBspjVvLgYC5Yd1tblRFzvqbRfpw4QYE5v0izs0A4VBVKqTIKp2zai741naoqVDSrqudC+M1+n5JHgjAn6qqknbhi3nxl9kxVFJLOuVPoREV5/EtZ4/arqwEU1TJn7nzuq2nVU5ssJ7VXgmAL0yQ+JtvR/Xi4QATfVjzZa8UZJA==";
        }
        else if (cardNumber.equals("4799479867216601")){
            return "AMPB6G0HNJlExpXjwtkGn2RmA9O9E5Q4DO/U9qyPM2duwvP5tVbmWyCR6NULFOdt3yEmPcRm6jedV4DvhRa/8dRzRkavCbv4yDqWdOmCU2KAQ06BNJX0EYVdGHlRpjB9NqLgUwWkUZWI5JaUfDpn6cIBQI38fMlohaM/rxwrKZnLYVfa41wuyABWoIuUUqq4XoBKH/CXZFOBfTEZLJy2tt3eHGIFksCBFjP9NpmSg1ig0W2ubySC9GB0XXxquP2gKi5hCgswwOiNxzh6/U5kSpfQ8bxOnrTjaR2rD2YXQhe4Y9PFpVKz52kJhHiCsFcr/X4wf1+Tcb5vI6BVETz0FA==";
        }
        else if (cardNumber.equals(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)){
            return "Z1Ye/OGKfZGNBzwnYwRNAyGqMRspzVMsocoxRIDPJLBnESsATVWMGWixiSbEq7ONs7mM8fV1NnplQbaU7bOHU4MPSfU98gCRD64jr1U4nC9fMkmv89r3GLZCNNWYPMBmseSahXYXeR9unhL5YNABpcZicvVuwsp0ZxKlBc7WV4toSz0nxOAw6t2VlcTODfDM9h19BtgLUK2WnSEHWToxJ3gb8zT4NSI3iRzExjImUTsJh6wNpRZDGXvpfIwn7GrLNY4nKfmwL7yahVoaSdqNrSPRiP7gZ/Np3Me0clyiStPqaEpPcnxvBM1MtPoSeNKo0FiAiOCbAto0TYsCd3aKoA==";
        }
        else if (cardNumber.equals(PaymentDTO.MASTER_CREDIT_CARD)){
            return "FasZMBR0ttuMv3JcVwYz0hbRIlvYgeV2p4IN8epdtajSwFko0oHDW8EcIOt2HtF2muFHkZOIFCDom5oViIG8N/cNQuHojEWPwc4LutazwzZ8pZDgMLlSsV/HoEkhGRrTrPyr+xZ2dcY4JXf6Bd9+kYmXeLTL1yewtCoBWuMPIY5dyK4ikC1rBwfgoxyyOqAH9TePmHzDQPIUCnYu9GpOBnqJ575RkUgh4gmlOIJxZPW7hm3m7b/uhewBvaaLSNtj9y988QxhpANraLgaEfW9nyizcCYcpRCbDFh/8YcPaYPc3H3BVWTThmfR57D9kMybeGqjkQaBM0XDjgIupWbNrA==";
        }
        else if (cardNumber.equals(PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN)){
            return "AiwVLcM1AoZsg9959AI3YaYPSiCeL1nVfYxGlsXXRKQ6zsmmISnBuGaXmW5QCFSUSymZFKlrc6XcKHxNgg96J0lilCRS7ZFdP5eDMbYY+rBgbCuEoxsRrpX8MUoEw9eAT59Hbp9IJao7xcWxuLhauMfkJmIECmjFhr+7Q6Xujit37h/xNCCSPWezM2VyCPBOD63oyOOgrHnkbrBMTN1q5YZVE7s5atTDeJ49ue/HJTJTJB7cpKddjpXZNYAN4pTqUx036l6HK3kbULlyfRTqRlNrTpjz/dczCx7MDy/7/bXDfmD84LS04iRSyV02G/H0/nL12GEjBRdSdehnIJduwQ==";
        }
        else if (cardNumber.equals(PaymentDTO.MASTERCARD_CC_BILL_PAYMENT)){
            return "QHE0HzFLNnQzYUFQ+gxBZwNM4MBmZJQqcI23TB3Zno0TXDbBz9q6Pt7dXmSVBDm7kOX2wbicuVeoX76MiWetlb4Z21+Z0GU6+fUc2NRtF80YepIVJ1JDrE7o/iQRr8VbropGe477fmIQFYgDI0ICY+zgfgg5taDkJi5bNG3juZC4sL0hnuqMhlhBP8MhYVFjqx02daIiT0rKF4/4If2RvYRTCvWhjwuYvojIQWUPj/Ux/eFHQyFFyqU9tbI5z/NE2MYlEt/0xe48UUTeRnADEd9DjJz3NSVUVNJ3YNwUUrFP5R3+AR8SDj1AjePhlKvdtxz/ts06bpnHA7IAGZ7gOQ==";
        }
        else if (cardNumber.equals(PaymentDTO.VISA_CC_BILL_PAYMENT)){
            return "UDd7sDSi+bLY4r+4788fvfDc34SoTgTVhg3Cy/Q3gaKuPJZ/fQToaAzvv5m3UUdwgCKDeWztjzxBvO3ACOX4qkNCpEyRud81Fj/OquSN8oGXvxSFImx6sNJnvBg8DJJaROlf9AiF58e09jymI+I9nWbuF4pdM/ri7do1pmy27TJThqpl6prYugzOme09V/nvmSWNB8Y7Uohx2YiU36MRdSMJ8bAUWVgoOBBJrx44p+R5IbFVhkaGag3GGkzGjB7aVNvLuwFYmYgXO1pINKT40hDmfnvaBeJzybLk2qKJZt5StY0lyfp9pfd9s3LWS+wIyclJXu2ReaRr+VPaHf4Q3g==";
        }
        else if (cardNumber.equals(PaymentDTO.INVALID_MASTER_CARD_CC_BILL_PAYMENT)){
            return "TP1HZEIlsY3PFrsCNK+cBmNgZX5tF27dtbks9YI55DSpCYwXKA2Thru/OB/ik4B0zNY7ohBaN4owEPXMXkADIuD3e5SxONPzLbWUtLHmDsKja1RQyejScNk+eeveNnaSpdAcL3c2E2cSxmmv6EYT9g9p+dQiRmWhPpHyvmMZzxdkr6XqUFdkMkU8nqZb0wq7Vd4/HUAe8q85swIZIQRgRNDrROwWyklNO2ad8Dll8EpaZuyi2pWkHTjUa/5mKwjQZQs6DluX0n8APrHHQkL26+tDC9GUA+pVbD3JEiNttiL657bAfOLysaYHZX6yCNtoLgTdjMRogXXztaMlFepNug==";
        }
        else if (cardNumber.equals(PaymentDTO.ICICI_CC_CARD)){
            return "EhiAqfqNRIIV5G7fVODoSj6v39TNoQ0JOAmYaYUOZU7y+c8/KUNVEIp1u38joJ0kfCDEej78aONdZMbQWE+4+dEV+42YuvGMDeBY9AUVC3cRkXiWqDpgxzjT3Nfw5tHhfLG62N37+da7Tc3AmltZWSN+cp90g9umwi9uYWpjtQ8cjl9EtaojEOt6aqYi4MGsHWYL10eQC48jUZswpLz+e4O+smKnIYjCfjzI623qJmh54BTHia/4qLYlgWhXHsLXoY/yxHaYM5q98Nfg7B54aMl8TJ/HMbbYm15nhTPCYu9HgAQ7J+hbeuYSc/kAoQjzWUxZ37YoytYUMpj9FLRIeg==";
        }
        else if (cardNumber.equals(PaymentDTO.PROMO_CC_CARD_ICICI)){
            return "UJlUXlwUyQOBk4gnrMU82EJUYo0Kswxcx51FJThBoJ6Ea1/FegVDfWqjZybxS8VqzR+729wAOMcT9j+6zPo+poS2taxWlfqgUTQysqRfSvIFqJPohK0Dq4CiP314r78UR7zPujp6pNGSXWTefNm95IKBWe1CjXpLjwiezRS9Do47FXkQTuBk4CI6YkzUximA5s4nI32xqXGKSIqgZUiJ5hEIx+wqzjtnhTB0T6U2HW5EEwKhF7ibk8XWrSBg97Jr/dzEP2jgq2tTPI5aRuzRMvyg9SERp7fBP/rKxOHvkIRKBWWaTaAjpmcZ8wpMDDOWFhMg43PfV97aPX/6oVhSuw==";
        }
        else if (cardNumber.equals(PaymentDTO.DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME)){
            return "YFdegOg14Yp+RBIHDAE4AMd6pcAX7JkwS5ryTWXk/Z1A74QouYP4CTnOcSLwREvHnlMuTBge430Wfz0u5VADhHh0sgTniMVQWiR7hwFTFH6IzmpMrPchLxQ/4370TPzre3+SfNHBsPWe2Qro4rsF1a+n1NUjh29suLo9SL/Ppw/QWdVuBC3JY4YcfCP/oWBkSoo3z5Jw6Ojok7wMm7OfgDyPpcro2/S810KGrp1+2HawUCjiDwKS1qGRcShuFnl5BH7SDqF8qPvUkVeboV0rLV0bytx0i5ZvFRjTW0GUAMN80LPPEWVz8ACzasAELeJ1U/Z37DyOcGxE3fXv8FfuUg==";
        }
        else if (cardNumber.equals(PaymentDTO.ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME)){
            return "TD3hQiBEn6f0bZ1fSI/3K5XstgJGqtGJYd2CW3bg7Rcke5hkzlKYfWha7t/47Xqnr2xD3ZjRpzqDorrBLozNs875H0OnJJ2kszjCEYXjK+p5SFKLYrdK3dN85PIRNmF4O5e6E2juYPr+KlGgJZZU5ZG3nYA+iLebo6vWq6CQmGE1iyip/p+sKiGENwU0tfNL5Na4B1al0DVaOVp3r5W8hJ3hPyO1mUI/zNHxcAAX8+f+HJrW88j8arL/yma1WZX8WHfOaqZrVr+dW7GI1NpNNcyt4Mj6dlWrnGBreKWcEfJMymZWEl8TMuf847dkExvOB9qsaSrKjlkOOdnitE3wQA==";
        }
        else if (cardNumber.equals(PaymentDTO.PREPAID_CARD)){
            return "DzQV48z7MR5U0Fnibmo5mwSLJEdGyo3jNWy17cfAJoJl0tcn5upsjxMyoOl+2vX6b99orvVvN/bTYmlDnwgRTineCdhCDBAyrzrsADy9lpMYLNBdQnSROZ+4bC3cB1/942QqRDZQ+6CbjREPw20PedgVtoOovSe6Mn56P2OFd1OEpmzP89TY4/1xwH3B7X024mDgynyRW9VX/pbUC994QUC0vkMQaj+FdWZpPAMNcOY5H39ZSPjVvbPTTCcId+b78+VEtTM1XH08wyhRRc1wUybE0dy9A9vswqeYjm4FAkWfiamLBKXR8ctJKgNBL5kOAMJwLxBB7tm2NkFQGxOyug==";
        }
        else if (cardNumber.equals(PaymentDTO.CORPORATE_PREPAID_CARD)){
            return "alcWU4z+m7tvO6/xFkMR1qKRaY2N/M76gaLp96x0OQ1SrYOYIE6GQmfbExUEAjtyW53kfkiAa8seTnUSZ5/6MDGj2MnE3DmgNWX4MM1crnDlZQ0uNrmtuUTX6oTE8Y1/oHP1H0lWFz0Gr+4AzcPPNUOp5+Du/5wtDQopiFjL+tc9F2O54Psx134LhNeOMA6BQjjuq/aKPBzzM6hljughmv5v4fjNcg/rSYG9CajDdsUVug0MRH+Ml2DFWxXdhAWXwWyUwK6TOHa4r3cdXt87R7Sk4c8FlL+pAywkIzHCRFohFAjNgZQRcBsiM4p4mnOA4SNyBjdwDGazNgKcj0YHmA==";
        }
        else if (cardNumber.equals(PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD)){
            return "SxNScMOt1222R7WEv3iH774EjEOKxifXKTQ/Ord7/+duv4+wd3dvHGcfqij3yVHIsAL2Ls6ad0l4t9YFqURQhhnrYf/9QtXA9M1i2eaZ9bAvNoWCjUipNHszgzpgplmvBUgbRRGsWaXUVWZk+h/vFQYsWvV7P+bkoTruGeeUgM9WNlTgQ8AGTFCBS1Cy7vaVjtbvyncvLGNsDu9sbZKVZXOMBxIu94lCesmrsgeQrGGWgfpXcIfnsRKdtLYZ67rEMoDXhFfYRrlhZz+mfU0RTdMDd6OFbm3pT1S9P1zkjJ62jZrVsE8G1zB3qfcJHhbfhQ5pMdVysGwHDRaro//Crg==";
        }
        else if (cardNumber.equals(PaymentDTO.CORPORATE_INDIAN_DC)){
            return "Oe1VJFtOsZ95CYJ2muYjClXIlajJ1XcOaIF0hCIm3IGdt4lCU/BHCnHQj9BT49s3su4J3MjBFCPgvf3uiJRdII/MEImFhL1jRPqPVatmksjAIv1BdN7ng/QltC/Ng01fojF2d2IRQTCFBVCPPVJ7qGI/R9cgi/iybXs3BWbrKEFJvOYTWP/F5FKsYzsLf19yk8qy/yrr4SpERNTY9piyGaSfE77Z/kfgqs4l2qEdZWThgMdJcNrHaGNcKTW1W7kuuh+oWKA2PnqPIjjY8xaeO0rN9dwT4wHMZ4heV5asLPtQErWUydgEWRc3bS7I0DavDU7vxYIhckgi2szlwCxAWg==";
        }
        else if (cardNumber.equals(PaymentDTO.CORPORATE_INDIAN_CC)){
            return "I7X+FEl8baGCgEacE3AZQlQzVD7YoDP1U7PzQgOjcWaDAAgd+u5e9GXfS6hrF9GIC8k03fLQdaXLzsOXu1s6gwrmcEYC0/2bwXIJudI3XjVtBePCUwHAoaEKGpvD/YfH7Hif4lv0zXcB943MaOaUQl271FU5LWxoke6zILWmnJGEHkyrCBE72J+LQv7Qqd5hpIM3Vt9BCVlFFE0Hb0gYMj3F0ZljhDsO/5YmQqn6uEMzGxF4RIdfvwAdE36WZZo4BcRdd+zU8LiOYJOL/EldXKyeSPkQ0jr1gOeAOPHUVya5Dw6QisO7cU97xj1DBwlPV1IIzvzD+AtZGqQW4ztygA==";
        }
        else if (cardNumber.equals(PaymentDTO.DC)){
            return "jIT9C6aBedvbkUdtt9AoM6ZlSz0zSq5S5DHqvo5Td7h71C2aU7Q4dz3ro41eMD5HLQcgrDImwt80WZGKiJmFtJwQQZjEEZg1hBp0QaV8DQtHG8LDu0Ct8hN6v6z70JK57zD2irpllh1TlUtoKSyiFdvhbHnVtcYJ3UTUiXuWErxG6u3EdmiCx5ef6s1CRZOnn0S6gp28z4tfvsLb7lusyojw5gDj5TDUFl+rDe6q9KorOT2UmVGUhqtfOI14OKHVJOwc69EkRwNkhp8mRe1p0ThzCRj04KVbULyLumb3uFxQSpF7TbuVcDI4KOKvNb2jRtb4MGl3eyX/uvQFf2izbw==";
        }
        else if (cardNumber.equals(PaymentDTO.CARDTOKEN)){
            return "NYYF0AEQhRGHvr5TfBqWqKnQ37ULrXXgoEFaKPeXyYiBELoqfAud/qRp9AgIQApKN3lQOlSK/j5aF8jJ+fTqeLAj2gVUZ3MWJ9UaKdPCYS5xJ7PYRHtu7KiWFLDswZ6i0J52qatYShFI3Vkru3FwJNJCw+W0aPBPeXl2IdeGiC4sVjLcXWh+mMyMkTPXNMMTS42Yd2RVkP22+z5fhCy2oO33T8t8TDn6SEPbVBikXus+U1hjmKiW8EsfXMhNy4M5Bb7Su+wZDh7Xp0IlWVtdk1JCiy6NhWprJHN8eqMV1vuOx2hwNQ8Kppi1CShsnNal8fWtO7aEQ+pjZ93mHEYAaQ==";
        }
        else if (cardNumber.equals("4761360075860501")){
            return "lFswUlbDY8qCqbQSzOxjoVensZkPipxA1Lu1CF/ih2ZyTM4lgotukPTNQW2Ji11TrzoJlngb1zBsm3KNQTjQbzQ1lzCBTU22y6qbDWVSY0ySDX8u/7TlKaQT7ygtPQTYC/beJl/HLaF4bY89OamfPIWiI2E7j9qTPo7tdwbBpIc7gXtwIj090Do4vH1DtOgDEwIJ+SNkYQ62t7/ZsTDd+vld87vTcK+pCU+fvXASqBY7HilA0U3SjpXEKnEM5X5BAzdXGMkXunT7lG61R0m31K+Hrzjp66qw5P7v/M+15JFJ1nmvbplirous7xkOpX8J6mvjeo7xZr0m9Pi11Vrefw==";
        }
        else if (cardNumber.equals("4718650100010336")){
            return "cH7kB6YOj24ZeIhBFB30t74vFnX9C7C56l/JNg7jPIaN4zHRCrPLE+YtRuIcXjApesETIuMmSOuQvWcuwOFnNRqxpHrqeXvvhNb+LPQVQjWr6fjgavucSrzuirmHG4S1+e0AFruTyEqmsp6h5SyKer9GGpRxgN+8kaHUs7hRiEVFjuEwuE1fwd5a58kekQKfvCAy/EtfIanBMnvy9zVsG4EAVm8RzUrmduy0YKa6dkcBIRKHbh6WlT90bJA6WEEmU2O5VeYoNbSmPPZjqqaU32vHjUK1ojoIp3kAyos/AOi4UL9C/AMW4b9kwlaWcw7Tm41U6k5+ajHcti5xa/FWzQ==";
        }
        else if (cardNumber.equals("4761360075860428")){
            return "D0xay5+5diCVqbOsK+PltUCZAY+GhRtEmcD4GiRj0ZnXwIF3WSdJlOuk2NpWtttqQBEd0YXIcB7jRvbH89iuTV8pHf4xFhbSza0FDqrNPnxv6ATN+JdeG5yQgnl+Ey0w8rpUpMtkvQl4lUo5jJmmMWmyGDvKFYcuOCJGoUO84+5tsiMucM+kE4SJkQDbqkO++w1XKHL3/zskckBkinVdRiBQVj3W2+GOpidKqbQLZNjlaQF+rKpqk0k7fBm9u6axT0q32sO/T2LS/H9ohJVOFGuPTEzk34hqnc1Bz2p7XLHmX5OXf86mD3WtsBW0w+yXBrIaY4+1ZiNOTWToWmIY0g==";
        }
        else if (cardNumber.equals("4761360075860436")){
            return "W84SHfztd+bYWnp31aibznEvcVTgo4ZATbWsfy9y3kVjz619GGqz3/B1x3oBXMLtpIO3EvQxSMHkJ1Jg7SlVmrFbEFC7D+luJIsZe1FB+44CCWylWEg4NP9KEH+cHzsUhj17q1XWA9Z477g/dpXivvRe1z7srsE0SQ/P7oif9NwwrWwE9YRyPtutNtf2YuVWR6J7xqD/r/lo0uBiVy30I3pNwzBf/XMx3HpZHu67QOI+EOi4sKS2dTUY0OB3OfdT89xD5TZINysUySh7G5xDyx+btMI5vtrcxXXXTNLEd2lzIuQXsm467vmXrxaqNWppdVje765429c/+PIc3BZS+Q==";
        }
        else if (cardNumber.equals("4761360075860444")){
            return "k3PXHJS9kqZXFLtG2KuY/DKElMZ5fgY8y25X1Rns8NmYLqFS8fMsmm5suAlQaGag02gyoT85UARm7CZx/XyhOxF5DU39/eYXmqMOA47WXxCnEN9wTfhrq4f+hkgYth9TjKDvPz8/OPxVBu/asJCUY1ZNMznKQ6iSaWPUN/XPjn0c3kIJAZb51xyeJqQxVnwm687o6H8W2MfLtsQiHOroHmII4PTKV9k0BrqZmhEkt4qzBoCxvbCP013zwdqhX8K81S0B90ta3/ViINpzAwpHBbaCLPcXHc6uEBoijebLlkyVu1QLlaezPAi5Nxz+Bqs7jIBAVVFAiwgyEuc2lt5w/Q==";
        }
        else if (cardNumber.equals("4761360075860451")){
            return "c624TV/HXRc9XAt1yBTYwvRlbmC3lcihUZl9bl78XnigmGY+O/AO0v/HtIBqlpgCqVsf03MNXIwWeT+hoHRRpAimvMlpwxUAG3CGHfMdtu/D+NtPj0goJKGUc/lYRnVsjNS2r+g3roNBY2Ln+0YhgXR/p6nhqyU2usBZIFvY37EbpjNyyPLwfX8q5/GJX12QElYGdH7fOeF/p/RoZXNDqgm5uL2Mf9akb0o3NVv1wbpeKm5MHv4TwhVgrXe/pDK2OQ9qjYFeNok5+REj43lPxvB/uLpXQmO4j/cykB0JHp4+EpsXhtZ3fwoz8Lq0HIObDRpaSKw9x9IDrqxrCEAV+g==";
        }

        else if (cardNumber.equals("4761360075860469")){
            return "edGlwdZOCQCz2UaExoaH70YSOXgCFwH7YkbB5jutWaH5ZR0uCsXplHwod+GyNlzLLfRt2/U3udrflXVrLLrRMFM8dyaYav42SA/XEO/ZbQzuPJ19bkil9YuhRTPq2MlLHh8JCxNXFR9SvGTZQ0qVlCJBfCMpfK0wH6NTS8N4wBDJ0sDSyWsGk1aAnNpcV5JTQyyiysVFYY24feupzuF3o+1Q1ll1xdVb/RFSoS3eu9cAnJ0PZ1XzW3a3++uKuKe4j88VfPnKmEvMpIqHCF3iYtIbEN259HBR6VO2u4mJkshaTIHrjcKpCtRlI/uvRJVS9jgzirALIckv1DsGgGwcLQ==";
        }

        else if (cardNumber.equals("4761360075860477")){
            return "KiNwZAUe7BFKXTyocVPsjizLxRpTHunZcYTn/mTzOFBdEEKZ4CB6Dn0LKaMz0CNxVnpzuIro8Q4r9Xqj3Rugf7eGb/khNq2cNjBlhGf3gIICIStrsB8/ODDuMAlymztoy+YFZIzDuxNTAqAkfcVNe4rEAvAAioMt83Dk9Cues/0slF40lo0rNIlbzqTUY0MXUZYk/ozT6uGvoJOtXNZUep6+ujJ0uXoGH/+v+nmPaxc3/VcwfVhBrujhLm+Denr+LzHp08BP8fem/ox3cmFYTXM+0d+U5j+tE3Zri3xXeQb3ks5NJn+gRfC2tkjPl2c/vyJihVzmghpvME5lvjuSwA==";
        }

        else if (cardNumber.equals("4761360075860485")){
            return "LQGhTbCcoJ+vv61gdu/Z9fDEmL+EwSBmu9SGw0Zv+OFcpDRXBLKPZ8a5Gg4oMjSGjp3Anm+s9lJLAIWwx6bLs41Sv7M+Dkt7rGSnYgp/h8nBGxpRN/7bQxJ3T8IkdQb5Ie+rYvwsALgr6JIie3T/slZHPdIjrCteYqYNS/OVFbtFyXxjVcP3VgE0swLuoBDNBcHB4hDaApD/HNwFjocqMSIjMnd27WP+5qAvwYz4dQQxcF4kcQruMrmbnfBNeWKGRhbVDHD1iyVBq8TXqzyEebNwE3SwU7K+GDVsO3VAC+VhP4ewwkCGLEnzB+xkRKNFvKjyvOvorIMJKRrVZF8R/w==";
        }

        else if (cardNumber.equals("4761360075860493")){
            return "ZMOIksy83/Y9r/zFu1iYgCbhskMpjw4Lu9ZlXOsJsBzy+sw3z8aJH+Eu9TIlEaW0th3G7ijIVWGIsbOYgJ5oBpR+QLNv6NC2r0jB10rPHIq/lm3ff31T3vhE/tgDSIWKfavzpstpin2/5Qj13nbE6zrPKpARbE6zn7Tmc13dhUEP6+b7VqqDpO5I3BuXXW4GqyJEV5uJtrgLvG3q8oxO1ax9Vz5RLi1TkiugI1ljlyVKtAazgzUrdB99jKj4hrW1WzSl/RrJMOgfdWJ6PUm3/Aiyjwt64LxRCPtJIszSofM6UY980Rk/WDnOv1xzMenhjsP530n4OaPwSZ+yHNE71g==";
        }

        else if (cardNumber.equals("4761360075860519")){
            return "IHKV0NcErvsu26QTqbzc8Sug1jATrlj6eooNaYb2SOgPOApns83ivX7uCiVoHuAt0QhTta5n1iap7fiaAThRtQYwlHqRoByydA02rDP//DWikFzFukgrtaGfiyJJM1QKyctYEUCERPYIyB0ZMuAxNzWw169kjXhP1g49BaN/jxZlBqecwB7aBWJinc2WdljQVQaF+2jr67aGzlqgUBUXOuc7vA6tAPvIuA7FsZgzNEcLHWgwP433Nq0Cjt1D3vLiq6EMpZEgkvld9FDd0p1CeDjgoXt3sRkjeOrJTcAc/93lMVe8VIr0IikhQk3HdgRlIvoOAeClK7zDw7zINkUYJA==";
        }

        else if (cardNumber.equals("4761360075860527")){
            return "g8JaUg8nkuiMLTPUiqx5E+K5VD40WvHh4rWIizO1qtixZhuZyusGrBVYVp5R2Uj2oXMaQfC5OOEugjM8fWczvLdbkkmCLNpQDd1UWqHG8B0ySzgXSAPacgNombrWYw5t2AO9q0vAWZpo+eDKQ2siQ0yZqbd8SscjDPeHYXQTTNAaJyFPGof4SR+Wa0lmHHgGsakjrTvipTVheHy4NdpU/VmwXDSTIm9qnKUbRwypYz634K/IrLIw28oMy9T9i8wmLSwt0Qsn7W8UurcmDW6fPbdFMyvfzndPK50w/TizIqqZdSfn8jkkJLZFL0X1FcyawqMuBBmnD73D1nRbMUYfpw==";
        }

        else if (cardNumber.equals("4761360075860535")){
            return "fHspnraVwePBPBKWXtbR8DpLuByR1Y90WI4Xfn7MjXu9SPt5RT9Hwu3iLZE3CY12QsSgflb7f39O6GF9jrinxs5P1fUC5blzutl2RbHBgUi1Zy/bRB6FBSMq80rsDw/Yu3rzbIqAnCoezaXrJAjRc6+KzkvdIbXvMEucv5SYXdh9S2ghf5Jd2XxcNib93EpLLJsgaEjxbeZUWICsAYZ45JejE0xO6IVMudnUu/QP5FE4OziIE8fOzuJ/kRok87JO0ZlOoauhSkbStiiwoTUdy2uRRmROAXmLOA3byuwSwqliNfYA3XPo8ZseFpHmUT7PUFXRYpImhethB1DswlUq6Q==";
        }

        else if (cardNumber.equals("4761360075860543")){
            return "D1/GU/ftsgZeX+z3JrZh2CYVpvYk26DIJhkShzrb1L0MYkosYwZBUXepDldIaALPg2GKl4XXF5AVm1LtXI58hjCf2yg5JLzecKhzsgHR4bgzxqfMWVL18dspMoE+y0OUzMXLo/mCiCUmCbb4G8R8QHZe35RTH3Bwx6qVaoHz6aZgOcl7pjoRIRzgFoCT1Z9WTAM1APmGQdf8T6F4S38Z0fO+I3x3tHIDyY4u8cJoaz2Kc7jW78KQg+TKWTvZalHrMM6BPE8cPgIZFsUbu8QOk2FQ2ruDxLvh5wfMvn0AgjnYbwFE14o+4C2wQ68VSKvBtk33s21gte0WemLRZcVSaw==";
        }

        else if (cardNumber.equals("4761360075860550")){
            return "kDkh/wAUJgGaXSO4nzqjJJ4jqD0u79/j+kc0TYBYQcEswk4BHuPSZh8P07LB4mLU9zW42pns+uZ5J+OfFqvFvoOFe0UlfV8bszSyRCZ3oJMA7IR64K/CFVpRu7fOjleTVrK0QdiJFYL8HkrbsR9XmB16gqXuIonGfRY91/zmy0k4bKcW9sE789kMR5lOoM91K5Mx/pDlPbNDis0u9U8gZdVq0+RndJyQGB4Cpiw7b+Zjmng79FdHhZgOxulW2J3ttSUhHMS+V7rBx2nlxvD7gTjduA+9fZ4JcC+PpfdB/vkc6Au4wl1DVuQfaORXB3aRuT/ou9dDhk+5I16X622WGQ==";
        }
        else if (cardNumber.equals("4761360075860568")){
            return "DqxfhV5Cu82etKB/f3bp2nF6a2RsyW50I+vZOtL+EQnrjQasw/xQu6jWyLHaOe2QZjyIPCeCMbDymWw+nJn55Ent7adlGNGAFiE1+ZU+veb9U8MlEHy1jmg2GsUJFchoRLkSPAgzwsxxrxy0bw/mOuUyfYGD3dYcKwwYKrEQrTvZXn7ygLyrMyc7oaQEpyOUva5W4ezbvhp4Fe4C3PQQzWlmIfkvAw6r9LXcy2aefvyY1zVoADynCoaX7jr0y/MUwMxqRR2ROOvNwQDjukR6VT8PervtSIKx0uWLuv4kR/5T/9mgNL4GOalwPpIqT5V2ofCXNK/6tPnHv1OLIZIjGw==";
        }
        else if (cardNumber.equals("4761360075860576")){
            return "PZDVZYn0eQdtkCti8V7j1iXGCldIlQ79O5mXcH+AKPcpt8t7Pee8IVme16vpQL6XzlloeDbTBlBt3uUWv7lgcNaPX1m2kDAUBv2nyCtUQiiMFe3FQNqCrsTeCmgGWdHSjm+WqauXUG+0Fad2E8XnLjMiPeSkcXYL1g3EcVKjg4E0XVWROBxvTszyXL9HLH3zO8v0Ue9S2g5pn8S28tVDuP6LLs/OmFsDe6+Yz5uTbawKLQJWWTRGVPX2swM73Cz/EKWuRz+lpSEX2kcFAGqGL4PXVvTBszGypJq2RVAto9eJslCk2YCGmhiL/yR/cyrk0s3RzQHbChiaYKeEvTS+Jw==";
        }
        else if (cardNumber.equals("4761360075860584")){
            return "IgHiQ0ox7KDBpqOjMdpBQbrineQ7UWZHKJ9/xQBCjkeYvvdNwb4nrqMFeHFrKgYXogG3jE+YPpCXQbHtea3FpgfH5aWQvzyb7KU2BMte7hdShDHjXsd4PTF3BVrtd6sRP07WIHSIvbPUKaeG2fUbGkV7u/diIKC2VMxA3bKidQV10xuJabl9tPE3Ns27CqPUVRaocoxSrv69jXPgMgtGlfOf34aSdCOZVcLJeEYXgM8EVS5X0aERcxOU4izXAfKKx1YfwNUwnZXS1UAzx5PdLRWBmWpjdUw08U0nLtU/Wa9TG0Gt/vYeX3Mxsf9vy9kROkmGt/dUgKC5CxP283UACg==";
        }
        else if (cardNumber.equals("4761360075860592")){
            return "CD3vZyMcKuyImLEoNKtCvzO9+Iwav5l5pJEq5T+a7VNc6H3fzaJlTDYFi1pLQ5cHNGN6rAOUi7vtiWU0IvcSqq9eLV7kRpDTqi3qBxShIC+Qj/gvWG+4DL+4SopySiavlgW20w0cLlJ1h33ShJgvNoHH/1pCqbPJuZ0tAFbTDRS1cw63UiDdHZQvxdrbBcsea8bdwZNTpExGfhrWzHQIzLZ0KPg6V2XvE0UkA3cpngc3G5vjA7+CobnhNd4xwuqWTpttZ6gR+x8gK/UDZy5Qdv8sWvGv0hRm71S5BmBteAqh1RiQv8HZA2ZC3kvfANDLXe9iDKxmXABpUbdACBOtsA==";
        }
        else if (cardNumber.equals("4761360075860600")){
            return "C8p4D299MJiLCL4/+41wZyMDxEyDD/kwYPuZi7sGvBxuTYoFZxHCMr2bGOp0OA2lMF89S9am5zvAf0JfsPfnJ6TU8XWm3QeLySxlOTWo3WmO1Dmp/kVrbPb0z2x74vI73La4LbWwmfyCK1m6S3HAEHG2TMK3snnvDLqglFaWFPGVmWaIPE96UPd4WlDYClzOK6YV7ZXZ99rrIRDJZO3zpP32dFARBIgLSFO8zI7FhSg/TNEEt0m6daqCmNPojEOZzGR8aWXWeYQtvK8eaEYf9TmeBVqAKvcmLshnw+3RQeC/LbrdDXOFtBU7FCymtDV5Dk7mjpasJO2vbez+mAJ9Jg==";
        }
        else if (cardNumber.equals("4761360075860618")){
            return "ZMMv4qmTBA44s3YqMZbptNt0bP/j27+IXDKrubnY4Hn0FXVHCLsrSL0xDfFUZjVujtFNZ24lLDej/80BY1D7PAXDo2fza9vEApIwaCOiQblAKNGN6q4sL0Uu7cA5bZxfVUcrNJCzI//cGN0XvznsiUitoxN6cnkOCRFft+e/kLi9rgJolsJJ88LDBX3YKucLn4RsrlWlCj20V7N2GVF6OSWKqzCBWFqeozpTZI3i/NpPn+MVNu01OJn+ZcNxF20jf5TzBFpygFH2Y2YerwZnwchRWa0553+gwogbeSq5HvNuMUfFly/Jt+S5H0lULRbA9T/ruHqZFUa1MfU/Fg20Qw==";
        }
        else if (cardNumber.equals("4761360075860626")){
            return "PXElOPAJraCZz+qRjxYbr0hkhT5nzuazJZBmRwXSUsRqKCpRr+CWV3wu+n1zwypcSKA0cC3k7Vvkzg8yOeiVQDz3FoAGpZFqDw59kcHG1J7AjJSnWOeSyjtcllCPa8pX7JJJEVrmOmn2z6+O1dHjt3G8Wqz3ev4PYtFghy9RNjlpS0fT9Ix1amNc4jzJd8yMiNLv0sWbmunkdUVNVGbBUSdjPngFrET2jNC8ayHy2H/yaKJk5oO6WH4m50GQijty+lo6saSUggjto0/H/LZnO8G50bv2nli+tcvWOrs+knyn09EWHHhucuGzsfZmYeED58F+i0irkIpfhK+DQPgdIA==";
        }
        else if (cardNumber.equals("4761360075860634")){
            return "PDjoaJlHQ++DEXwPIOSUUOepbVWH4TnxlYSitLCjyk3YfBLQ0eAjguzWOcf+owr1a3N6G3kA/JWLAWk0lIpkrXBCRez7dO30B1wFierak24CiTbuqKMu/xsFB+amw4yVM9ouHnSZeu2/NpR3Cdz+DcAh1umuXBExKlzqSodxf/zLqo4yTb5pIK/D7VX9GdW2PbxxZHQ360txQVe5NMzW/teRq7CbxNu9vCKgt4Ug86MJIHuJ4QqEeZnHe93j36JH9+pq9eGJ6ZlVfG5bKX8W38h9bC/bXetAd6+qgFbVwsRxewJSzoaQc1OeWKfAhDc2nfni7BGy2Z75oSSCIc315w==";
        }
        else if (cardNumber.equals("4761360075860642")){
            return "P5rYo/htgJK+t+F6kaQvI0FxgDpDPioNVz6MxUAh5iXDqZ5jJETWhvmWy15a/WHrn/xOGmD0IUwY63mFfLFrxNA6epJrV/HOgrCHI7wo20injbMDteTWF+wjLOninBSCW9AeRn8YXqr+lK5fU/uh5Um6rP41epwfyJrzTOuFAX6Wma2dBl0vi+lcKaGK3YKs4TgyopLcc9XnLI7gZ0c9Qr9Qe95SpKVIq54LWoChWi2wn8UXPKZn2xgefB8RIBPGdvnCWsrp6RJrhRQdSM61PYJ1FwmivwFIm28yv2ItQIbuG0czWRxDaptYGrclc1Gmdd0lr9tKBNpFwCB9D+FFTw==";
        }
        else if (cardNumber.equals("4761360075860659")){
            return "ILQzFvhK+pPkUAK3IvpNSzmnGvuwqcIvySyqKEg1pl+oA+/mD7zTz24O582r3Y50OTTQJPwDtkmyRht+A+WGURNgrRutjU81wfFR7wisFbbAw+5D50Y2tSYzAZ8LxCzdZzSXJweis1AXBv9RJMcBzPr7XUg/V43RQhLSJnX03a5YRtOHuv1iiR7V3xxmILrrTVGjwiGI0HmMBmAfEQHpnwJCiNKZfoyvtWuiEHSHfE72kg25S+5sqv/1Z8He5nwCeHPyc7si8nGiY8tPQIf6RBZTq+e1kMyqO1cwL3y9LZHN7un4Oo4QOoBUzLHxFRJUprKClGCs/PmlvfeDiVkXhQ==";
        }
        else if (cardNumber.equals("4761360075860667")){
            return "Z/bSlxES/hiru3fRwB356TQSK6drFGPdCXWm6rEMO5JJyM5PMOxSIPqPkOJKcVTqJiRDBH9SBHz1KoV+FmXySfNtyPyNbPEIRU+FYNvev+U6IJJ9XVzXU9ibpfnYTlgl5NW4zah0Uf4rV3KcoYLsqyNgxYsnlAcZceeQEanj+L+wFWGUcQkbVwKZ2+jPcBhueKk37ROCQItxln0031mRp8tqsyJjEwPL9Tm7MwIiZNndcZljUvjoEbwPYUUZVlhbfjx7SIqG8jUl31ZHO8foHjqZLPn7if6/kaOR9zx5zBUx/y6IAmusrdpjEaVRJQuIsrdY5RZcSxtjZKrLEDTFhA==";
        }
        else if (cardNumber.equals("4761360075860675")){
            return "ZLlyyt2nI3In+0dnVO4fT4DEwbrm5W2tK1sx6GBQeJjS+0cb1FDNXVpHMEqleb8oiP8xpsxjkux+wRTT71OjpflEV1GULeFBLigvQ0yu7WaJILp2ghd7G/e1bh9JDMxyKl5zcvyqyFwJ/jK+8g/JJ2NLvDSzD2BTaDivBo8OAsyD1utX55yJKky1neWf9cJ5prc9DIr9L9Z08e6KRicuT77WHIoHIKtniz1hweaMLpiq3A8iu9K6ISHEQlhECuqGqfwmwXBQ2YOrh50+m6wF7hMkp/vb49vTvQ1N/RccSVYIqqwPnH3BYTUphnSy/exD/GUfXVE3xReWYsYubsK6MA==";
        }
        else if (cardNumber.equals("4761360075860683")){
            return "Eoxe0o5pZW65UP7sjjdhWxe7rSLj42Sy6BOSPH01jAcyRhxOoDV1v2SyCrB91Y6Avs8drQXMmj6ukPCedmZ/eTtyC8NG5GzgIoBIPI5kBl6LTLIKEcc6Q6iH/h/o+yOq7na/QM0f1w/e51PrcU7uOxn9XrJzRDd31X/5wsEpBqD+aqFPk6v1h3Jtnl20Vo8k4oclykQk8B2GFQ/a3KWKUmjeSKM9KPfZ4pwr+IqSn3mTQYeIKoYnNpwbqdJONuE30GOzSR5PdV9eHBKVr+XY/Gjbhska0mWpUGtUFBMiy6v9M1ca2yeKGdlcPvEVIdh3PgEEiFNC2efj0RWGhaG5ow==";
        }
        else if (cardNumber.equals("4761360075860691")){
            return "a++fw/fYass1s6G3t/cuFwCsCos9285w1hvBBKLLxRSym3ND9Ts3s2sO10w9MyJ03VkB/5G4GaZG44ai96q4kaDANAYA5JecAUkJTwKPaqfrFAQ7rKkB3MMQreNlDD6+nCDI5C9Tey/rI1n7CbDTa+Mn/VTnAENfwM1YNYq788kREIVLvcOyEqIAK8/z0cv4JGVUagPnYlCZI+MgsTpHbDGK4ZWgvNQO3RYPIpqY16Tye1zHAuiPmsWj0miKiro68KckUWUGqdForda4Ig2De7AqjjgYZTlOUOoDBX7lfSj9iM6w/uc3VM7PrDtMEiUPLwjtKlgSaHKXWU6xn4zXVA==";
        }
        else if (cardNumber.equals("4761360075860709")){
            return "OhJG2bEswkCVenOJir3KwgaYpKXf5LD+12AOTMNYxjXNHBf5MmsOO9TdBqtH1rpOaiy/OVqfPjhR9enqAsyboFMlgnX2Qg0REWS1IMO5jBf3eleWI+HOH7h8NlTVtIlPrxDKzLmeYz/MEK5utnDJsro+kmWPjJzS9pVwWFexngTsCIRJoL5pOLQpKaCQWE5I7GfRoRLsAGem/UAHPZYtqil5hywZDiLbrQbj46Y2hzP0NnyYsT87PphUkx8cWB+fNDCic5O6woxdXTQQCusxngx0OUnji29gL3STJBvXvfQs4JIk3NwwQ0EkZmSBrs6uDdJ8DghreTdS39x9B6NLmQ==";
        }
        else if (cardNumber.equals("4761360075860717")){
            return "a0AspZJ3WbFpfGwQEodKR7dw5pKo5aTWX5P+Sdcl+2YhsvJcJ8VzyheJg0caQbDcXG9xrNP7laacFM20ZFU1/PTeY7pxfMdYOx2Ro4rGtuMmslLmg8eThCNZbHM/roLsA6zGMP8JvD6gvsMVKS64i93UnjmB10dhFYqFIUjdazaj1ecIgFChCEwy9+1SU0LZorgFqWuO+Ej4qzx8rcdv6OegGYNrc46tqV7kgFzQACOWb95fJRKdSlmyN6q+eaAyz63gL0YRJVlJwTU6dEzQ5B3nASyK32TCmxw0SXKhf4Bnu9Bihlj/TX7vHwZA4e+ia4wT2seqYwXiJDZYxhg19A==";
        }
        else if (cardNumber.equals("4761360075860725")){
            return "a0AspZJ3WbFpfGwQEodKR7dw5pKo5aTWX5P+Sdcl+2YhsvJcJ8VzyheJg0caQbDcXG9xrNP7laacFM20ZFU1/PTeY7pxfMdYOx2Ro4rGtuMmslLmg8eThCNZbHM/roLsA6zGMP8JvD6gvsMVKS64i93UnjmB10dhFYqFIUjdazaj1ecIgFChCEwy9+1SU0LZorgFqWuO+Ej4qzx8rcdv6OegGYNrc46tqV7kgFzQACOWb95fJRKdSlmyN6q+eaAyz63gL0YRJVlJwTU6dEzQ5B3nASyK32TCmxw0SXKhf4Bnu9Bihlj/TX7vHwZA4e+ia4wT2seqYwXiJDZYxhg19A==";
        }
        else if (cardNumber.equals("4761360075860733")){
            return "BZLzR2Gkn4HL+nW3W4Xu1rgFrdAvscOb6L77lzTfwmd9cOOf26EH8bPDj9bg0KM35XFJiS0a+OH3Aq0jec0m6yYsIbSiJsOFkYylHbNYq4GdXcYG/S3VXbFgSXq1vSJxE8e1daFA/HgDVPiL5I8SlXpgmKtP9Yc9No3F29mr8ph1PiVnWQYYagHGH0k8QZvcLDEFWtLFPkHmHvmeE3dbBa2+H9x53seSyxemOs2LtvXJ/KCwXnV3Ku5zYz4LtvxItAuWCxVfVNlLqJo0R0QxQ2Z8RTbpHcEPwCEpWEOWvGFQnanuCbxOZbOdkm73pTx4EtkfDVpYOVTj6LFevuHApA==";
        }
        else if (cardNumber.equals("4761360075860741")){
            return "XDxYTga7mur/bVgZot9deAJ+fAReUN02uuAEFbtf7UlbfNVDWs+73/nK3RY8/Vzap9Ht/QC6Y/U1j4dHI53PaOxWJK5ympZXjMFebv6ITBQG/T24aEduwWWaDyCteD/+VIs/0gE86S+GJiGD9gm0YnCYP2Hg27Fg4n3Qlc3AixqHbj/AM2mqDamUWjUzQJqCEN9ULBDJromWRH7adIVK8c/pg0vLGuKzMJuIpnX5fHA8rEXaDrRPRc4AocFpa4LqYV7PWANFL2J6lw7OH8v70HGI6xsBKTWKOLv1w6aWAC02xi3x3wkP9aeCEAkQ/gd4KZDZIw1UWxgbvIQ+3gDnUw==";
        }

        return "";
    }
}

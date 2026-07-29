"""Build and apply a substitution cipher decoder for the obfuscated Java files.
The cipher is a simple monoalphabetic substitution on [a-z] and [A-Z].
"""
import string

# Known mappings from comparing obfuscated text with expected Java text
# Each pair: (cipher_char, plain_char)
known_pairs = [
    # package dev.molang.iamzombieq.gameplay
    ('l', 'p'), ('t', 'a'), ('b', 'c'), ('k', 'k'), ('g', 'g'), ('v', 'e'),  # package
    ('v', 'd'), ('v', 'e'), ('v', 'v'),  # dev (vvv)
    ('b', 'm'), ('l', 'l'), ('t', 'a'), ('v', 'n'), ('g', 'g'),  # molang (bbltvg)
    ('t', 'i'), ('b', 'm'), ('z', 'z'), ('b', 'o'), ('b', 'b'), ('v', 'e'), ('q', 'q'),  # iamzombieq (ttbzbbbtvq)
    ('g', 'g'), ('t', 'a'), ('b', 'm'), ('v', 'e'), ('l', 'p'), ('l', 'l'), ('y', 'y'),  # gameplay (gtbvllty)
    # import (tblbvt)
    ('t', 'i'), ('b', 'm'), ('l', 'p'), ('v', 'r'),  # import (tblbvt)
    # public class (lubltb ttvtl)
    ('l', 'p'), ('u', 'u'), ('l', 'l'), ('t', 'i'),  # public (lubltb)
    ('t', 'c'), ('t', 'l'), ('v', 'a'), ('l', 's'),  # class (ttvtl) - wait ttvtl has 5 chars for 5-letter "class"
    # static (sttttb)
    ('s', 's'), ('t', 't'), ('t', 'a'), ('t', 'i'), ('b', 'c'),  # static (sttttb)
    # void (vbtv)
    ('v', 'v'), ('b', 'o'), ('t', 'i'), ('v', 'd'),  # void
    # if (tt)
    ('t', 'i'), ('t', 'f'),  # if
    # return (vvtuvv)
    ('v', 'r'), ('v', 'e'), ('t', 't'), ('u', 'u'), ('v', 'r'), ('v', 'n'),  # return
    # null (vull)
    ('v', 'n'), ('u', 'u'), ('l', 'l'), ('l', 'l'),  # null
    # boolean (bbblvtv)
    ('b', 'b'), ('b', 'o'), ('b', 'o'), ('l', 'l'), ('v', 'e'), ('t', 'a'), ('v', 'n'),  # boolean
    # private (lvtvttv)
    ('l', 'p'), ('v', 'r'), ('t', 'i'), ('v', 'v'), ('t', 'a'), ('t', 't'), ('v', 'e'),  # private
    # new (vvw)
    ('v', 'n'), ('v', 'e'), ('w', 'w'),  # new
    # this
    # String
    ('S', 'S'), ('t', 't'), ('v', 'r'), ('t', 'i'), ('v', 'n'), ('g', 'g'),  # String (Stvtvg) 
    # ItemStack (ttvbSttbk)
    ('t', 'I'), ('t', 't'), ('v', 'e'), ('b', 'm'), ('S', 'S'), ('t', 't'), ('t', 'a'), ('b', 'c'), ('k', 'k'),  # ItemStack (ttvbSttbk)
    # true
    ('t', 't'), ('v', 'r'), ('u', 'u'), ('v', 'e'),  # true (tvuv)
    # false
    ('t', 'f'), ('b', 'a'), ('l', 'l'), ('s', 's'), ('v', 'e'),  # false (tblsv)
    # Object (bbjvbt)
    ('b', 'O'), ('b', 'b'), ('j', 'j'), ('v', 'e'), ('b', 'c'), ('t', 't'),  # Object
    # for (tbv)
    ('t', 'f'), ('b', 'o'), ('v', 'r'),  # for
    # Zombie (Zbbbtv)
    ('Z', 'Z'), ('b', 'o'), ('b', 'm'), ('b', 'b'), ('t', 'i'), ('v', 'e'),  # Zombie
    # ZombieFoodEvents class (Zbbbtvtbbvvvvvts)
    ('b', 'o'), ('b', 'm'), ('b', 'b'), ('t', 'i'), ('v', 'e'),  # -ombie- (repeated pattern)
    # LivingEntity (lltyvv)
    ('l', 'L'), ('l', 'i'), ('t', 'v'), ('y', 'i'), ('v', 'n'), ('v', 'g'),  # Living (lltyv) then Entity
    ('v', 'E'), ('v', 'n'), ('t', 't'), ('t', 'i'), ('y', 'y'),  # Entity (vttvy) 
]

# Let me just collect the best guesses for each character
# and figure out the cipher by looking at what's most common

# Actually, let me look at plain->cipher mapping to identify patterns
plain_to_cipher = {}
for cipher, plain in known_pairs:
    if plain not in plain_to_cipher:
        plain_to_cipher[plain] = []
    if cipher not in plain_to_cipher[plain]:
        plain_to_cipher[plain].append(cipher)

# Group ambiguous mappings
for p, c_list in sorted(plain_to_cipher.items()):
    if len(c_list) > 1:
        print(f"WARNING: '{p}' maps to multiple ciphers: {c_list}")

# Build cipher_to_plain (pick first mapping for each, warn later)
cipher_to_plain = {}
for cipher, plain in known_pairs:
    if cipher in cipher_to_plain and cipher_to_plain[cipher] != plain:
        print(f"CONFLICT: '{cipher}={cipher_to_plain[cipher]}' vs '{cipher}={plain}'")
    cipher_to_plain[cipher] = plain

print("Cipher mapping so far:")
for c, p in sorted(cipher_to_plain.items()):
    print(f"  '{c}' -> '{p}'")
    
# Encode a test
test_words = ["Hello", "World", "new", "null", "true", "false"]
for w in test_words:
    encoded = ''.join(plain_to_cipher.get(ch, ['?'])[0] if ch in plain_to_cipher else '?' for ch in w)
    print(f"  {w} -> {encoded}")

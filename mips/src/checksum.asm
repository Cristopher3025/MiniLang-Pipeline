.data

# File paths are relative to the project root, which must be the MARS working directory.

inputFile:
    .asciiz "data/output/resultado.txt"

outputFile:
    .asciiz "data/output/firma.txt"


resultKey:
    .asciiz "RESULT="

operationsKey:
    .asciiz "OPERATIONS="


buffer:
    .space 2048


numberBuffer:
    .space 32


signatureText:
    .asciiz "SIGNATURE="


newline:
    .asciiz "\n"


successMessage:
    .asciiz "Firma generada correctamente.\n"

errorOpen:
    .asciiz "Error: no se pudo abrir resultado.txt\n"

errorResult:
    .asciiz "Error: no se encontro RESULT=\n"

errorOperations:
    .asciiz "Error: no se encontro OPERATIONS=\n"


.text
.globl main


main:

    li $v0, 13
    la $a0, inputFile
    li $a1, 0
    li $a2, 0
    syscall

    move $s0, $v0

    bltz $s0, file_open_error


    li $v0, 14
    move $a0, $s0
    la $a1, buffer
    li $a2, 2047
    syscall

    move $s1, $v0

    la $t0, buffer
    addu $t0, $t0, $s1
    sb $zero, 0($t0)


    li $v0, 16
    move $a0, $s0
    syscall


    la $a0, buffer
    la $a1, resultKey

    jal find_key

    beq $v0, $zero, result_not_found

    move $a0, $v0

    jal ascii_to_integer

    move $s2, $v0


    la $a0, buffer
    la $a1, operationsKey

    jal find_key

    beq $v0, $zero, operations_not_found

    move $a0, $v0

    jal ascii_to_integer

    move $s3, $v0


    # checksum = (RESULT XOR OPERATIONS) + 17
    xor $t0, $s2, $s3

    addi $s4, $t0, 17


    move $a0, $s4
    la $a1, numberBuffer

    jal integer_to_ascii

    move $s5, $v0
    move $s6, $v1


    li $v0, 13
    la $a0, outputFile

    li $a1, 1

    li $a2, 0
    syscall

    move $s7, $v0

    bltz $s7, file_open_error


    li $v0, 15
    move $a0, $s7
    la $a1, signatureText

    li $a2, 10

    syscall


    li $v0, 15
    move $a0, $s7
    move $a1, $s5
    move $a2, $s6
    syscall


    li $v0, 15
    move $a0, $s7
    la $a1, newline
    li $a2, 1
    syscall


    li $v0, 16
    move $a0, $s7
    syscall


    li $v0, 4
    la $a0, successMessage
    syscall

    j exit_program



# Searches a null-terminated string for a key.
# Input: $a0 = text address, $a1 = key address.
# Output: $v0 = address after the key, or 0 if not found.

find_key:

    move $t0, $a0


find_key_loop:

    lb $t1, 0($t0)

    beq $t1, $zero, find_key_not_found

    move $t2, $t0
    move $t3, $a1


compare_key_loop:

    lb $t4, 0($t3)

    beq $t4, $zero, find_key_found

    lb $t5, 0($t2)

    beq $t5, $zero, find_key_not_found

    bne $t4, $t5, next_position

    addi $t2, $t2, 1
    addi $t3, $t3, 1

    j compare_key_loop


next_position:

    addi $t0, $t0, 1

    j find_key_loop


find_key_found:

    move $v0, $t2

    jr $ra


find_key_not_found:

    move $v0, $zero

    jr $ra



# Converts decimal digits to an integer.
# Input: $a0 = address of the first digit.
# Output: $v0 = converted integer.

ascii_to_integer:

    li $t0, 0


ascii_integer_loop:

    lb $t1, 0($a0)

    li $t2, 48
    blt $t1, $t2, ascii_integer_end

    li $t2, 57
    bgt $t1, $t2, ascii_integer_end

    addi $t1, $t1, -48

    mul $t0, $t0, 10

    add $t0, $t0, $t1

    addi $a0, $a0, 1

    j ascii_integer_loop


ascii_integer_end:

    move $v0, $t0

    jr $ra



# Converts a non-negative integer to decimal ASCII.
# Input: $a0 = integer, $a1 = output buffer.
# Output: $v0 = first character address, $v1 = character count.

integer_to_ascii:

    addi $t0, $a1, 31

    sb $zero, 0($t0)

    move $t1, $a0

    li $t2, 0


    bne $t1, $zero, integer_ascii_loop

    addi $t0, $t0, -1

    li $t3, 48
    sb $t3, 0($t0)

    li $t2, 1

    j integer_ascii_end


integer_ascii_loop:

    li $t3, 10

    div $t1, $t3

    mfhi $t4
    mflo $t1

    addi $t4, $t4, 48

    addi $t0, $t0, -1
    sb $t4, 0($t0)

    addi $t2, $t2, 1

    bne $t1, $zero, integer_ascii_loop


integer_ascii_end:

    move $v0, $t0
    move $v1, $t2

    jr $ra



file_open_error:

    li $v0, 4
    la $a0, errorOpen
    syscall

    j exit_program


result_not_found:

    li $v0, 4
    la $a0, errorResult
    syscall

    j exit_program


operations_not_found:

    li $v0, 4
    la $a0, errorOperations
    syscall



exit_program:

    li $v0, 10
    syscall
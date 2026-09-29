.data

# ---------------------------------------------------------
# Rutas de archivos
# IMPORTANTE:
# Ejecutaremos MARS tomando como directorio de trabajo
# la raiz MiniLang-Pipeline.
# ---------------------------------------------------------

inputFile:
    .asciiz "data/output/resultado.txt"

outputFile:
    .asciiz "data/output/firma.txt"


# ---------------------------------------------------------
# Textos que vamos a buscar dentro de resultado.txt
# ---------------------------------------------------------

resultKey:
    .asciiz "RESULT="

operationsKey:
    .asciiz "OPERATIONS="


# ---------------------------------------------------------
# Buffer donde MARS guardara resultado.txt
# ---------------------------------------------------------

buffer:
    .space 2048


# ---------------------------------------------------------
# Buffer para convertir la firma numerica a texto
# ---------------------------------------------------------

numberBuffer:
    .space 32


# Texto que escribiremos en firma.txt
signatureText:
    .asciiz "SIGNATURE="


newline:
    .asciiz "\n"


# Mensajes para consola
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

    # =====================================================
    # 1. ABRIR resultado.txt
    # =====================================================

    li $v0, 13
    la $a0, inputFile
    li $a1, 0
    li $a2, 0
    syscall

    # $v0 contiene el descriptor del archivo
    move $s0, $v0

    # Si es negativo, hubo error
    bltz $s0, file_open_error


    # =====================================================
    # 2. LEER resultado.txt
    # =====================================================

    li $v0, 14
    move $a0, $s0
    la $a1, buffer
    li $a2, 2047
    syscall

    # Cantidad de bytes leidos
    move $s1, $v0

    # Colocamos '\0' al final del contenido
    la $t0, buffer
    addu $t0, $t0, $s1
    sb $zero, 0($t0)


    # =====================================================
    # 3. CERRAR resultado.txt
    # =====================================================

    li $v0, 16
    move $a0, $s0
    syscall


    # =====================================================
    # 4. BUSCAR "RESULT="
    # =====================================================

    la $a0, buffer
    la $a1, resultKey

    jal find_key

    # find_key devuelve en $v0 la direccion
    # que aparece inmediatamente despues de RESULT=
    beq $v0, $zero, result_not_found

    move $a0, $v0

    jal ascii_to_integer

    # Guardamos RESULT
    move $s2, $v0


    # =====================================================
    # 5. BUSCAR "OPERATIONS="
    # =====================================================

    la $a0, buffer
    la $a1, operationsKey

    jal find_key

    beq $v0, $zero, operations_not_found

    move $a0, $v0

    jal ascii_to_integer

    # Guardamos cantidad de operaciones
    move $s3, $v0


    # =====================================================
    # 6. CALCULAR CHECKSUM
    #
    # checksum = (RESULT XOR OPERATIONS) + 17
    # =====================================================

    xor $t0, $s2, $s3

    addi $s4, $t0, 17


    # =====================================================
    # 7. CONVERTIR CHECKSUM A TEXTO
    # =====================================================

    move $a0, $s4
    la $a1, numberBuffer

    jal integer_to_ascii

    # integer_to_ascii devuelve:
    #
    # $v0 = direccion donde empieza el numero
    # $v1 = cantidad de caracteres

    move $s5, $v0
    move $s6, $v1


    # =====================================================
    # 8. CREAR firma.txt
    # =====================================================

    li $v0, 13
    la $a0, outputFile

    # 1 = escritura
    li $a1, 1

    li $a2, 0
    syscall

    move $s7, $v0

    bltz $s7, file_open_error


    # =====================================================
    # 9. ESCRIBIR "SIGNATURE="
    # =====================================================

    li $v0, 15
    move $a0, $s7
    la $a1, signatureText

    # SIGNATURE= tiene 10 caracteres
    li $a2, 10

    syscall


    # =====================================================
    # 10. ESCRIBIR EL NUMERO
    # =====================================================

    li $v0, 15
    move $a0, $s7
    move $a1, $s5
    move $a2, $s6
    syscall


    # =====================================================
    # 11. ESCRIBIR SALTO DE LINEA
    # =====================================================

    li $v0, 15
    move $a0, $s7
    la $a1, newline
    li $a2, 1
    syscall


    # =====================================================
    # 12. CERRAR firma.txt
    # =====================================================

    li $v0, 16
    move $a0, $s7
    syscall


    # =====================================================
    # MENSAJE DE EXITO
    # =====================================================

    li $v0, 4
    la $a0, successMessage
    syscall

    j exit_program



# =========================================================
# FUNCION: find_key
#
# Busca una cadena dentro del buffer.
#
# Entrada:
#   $a0 = direccion del texto
#   $a1 = direccion de la clave
#
# Salida:
#   $v0 = direccion inmediatamente despues de la clave
#         0 si no se encontro
# =========================================================

find_key:

    move $t0, $a0


find_key_loop:

    lb $t1, 0($t0)

    # Fin del texto
    beq $t1, $zero, find_key_not_found

    move $t2, $t0
    move $t3, $a1


compare_key_loop:

    lb $t4, 0($t3)

    # Llegamos al final de la clave:
    # significa que encontramos coincidencia completa
    beq $t4, $zero, find_key_found

    lb $t5, 0($t2)

    # Si termina el texto, no hay coincidencia
    beq $t5, $zero, find_key_not_found

    # Si los caracteres son diferentes,
    # probamos desde la siguiente posicion
    bne $t4, $t5, next_position

    addi $t2, $t2, 1
    addi $t3, $t3, 1

    j compare_key_loop


next_position:

    addi $t0, $t0, 1

    j find_key_loop


find_key_found:

    # $t2 ya apunta justo despues de la clave
    move $v0, $t2

    jr $ra


find_key_not_found:

    move $v0, $zero

    jr $ra



# =========================================================
# FUNCION: ascii_to_integer
#
# Convierte, por ejemplo:
#
# "60\n"
#
# en:
#
# 60
#
# Entrada:
#   $a0 = direccion del primer digito
#
# Salida:
#   $v0 = numero entero
# =========================================================

ascii_to_integer:

    li $t0, 0


ascii_integer_loop:

    lb $t1, 0($a0)

    # Si caracter < '0', terminamos
    li $t2, 48
    blt $t1, $t2, ascii_integer_end

    # Si caracter > '9', terminamos
    li $t2, 57
    bgt $t1, $t2, ascii_integer_end

    # ASCII -> numero
    addi $t1, $t1, -48

    # acumulador = acumulador * 10
    mul $t0, $t0, 10

    # acumulador += digito
    add $t0, $t0, $t1

    # siguiente caracter
    addi $a0, $a0, 1

    j ascii_integer_loop


ascii_integer_end:

    move $v0, $t0

    jr $ra



# =========================================================
# FUNCION: integer_to_ascii
#
# Convierte un entero positivo a caracteres ASCII.
#
# Ejemplo:
#
# 80 -> "80"
#
# Entrada:
#   $a0 = numero
#   $a1 = buffer
#
# Salida:
#   $v0 = direccion inicial del texto
#   $v1 = cantidad de caracteres
# =========================================================

integer_to_ascii:

    # Trabajamos desde el final del buffer
    addi $t0, $a1, 31

    # Terminador nulo
    sb $zero, 0($t0)

    move $t1, $a0

    li $t2, 0


    # Caso especial: numero 0
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

    # Digito -> ASCII
    addi $t4, $t4, 48

    # Guardamos de derecha a izquierda
    addi $t0, $t0, -1
    sb $t4, 0($t0)

    addi $t2, $t2, 1

    bne $t1, $zero, integer_ascii_loop


integer_ascii_end:

    move $v0, $t0
    move $v1, $t2

    jr $ra



# =========================================================
# ERRORES
# =========================================================

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



# =========================================================
# FINALIZAR
# =========================================================

exit_program:

    li $v0, 10
    syscall
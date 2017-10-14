//init
@256
D=A
@SP
M=D

//call Sys.init 0
@RETURN_ADDR_0
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@7777
M=1
M=0
@Sys.init
0;JMP
(RETURN_ADDR_0)

//function Sys.init 0
(Sys.init)
D=0

//push constant 4000
@4000
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop pointer 0
@SP
M=M-1
@SP
A=M
D=M
@THIS
M=D

//push constant 5000
@5000
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop pointer 1
@SP
M=M-1
@SP
A=M
D=M
@THAT
M=D
@7777
M=1
M=0

//call Sys.main 0
@RETURN_ADDR_1
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@7777
M=1
M=0
@Sys.main
0;JMP
(RETURN_ADDR_1)

//pop temp 1
@SP
M=M-1
@SP
A=M
D=M
@R6
M=D

//label LOOP
(Sys.init$LOOP)

//goto LOOP
@Sys.init$LOOP
0;JMP

//function Sys.main 5
(Sys.main)
D=0
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
M=D
@SP
M=M+1
@SP
A=M
M=D
@SP
M=M+1

//push constant 4001
@4001
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop pointer 0
@SP
M=M-1
@SP
A=M
D=M
@THIS
M=D

//push constant 5001
@5001
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop pointer 1
@SP
M=M-1
@SP
A=M
D=M
@THAT
M=D

//push constant 200
@200
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop local 1
@LCL
D=M
@1
D=D+A
@addr
M=D
@SP
M=M-1
@SP
A=M
D=M
@addr
A=M
M=D

//push constant 40
@40
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop local 2
@LCL
D=M
@2
D=D+A
@addr
M=D
@SP
M=M-1
@SP
A=M
D=M
@addr
A=M
M=D

//push constant 6
@6
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop local 3
@LCL
D=M
@3
D=D+A
@addr
M=D
@SP
M=M-1
@SP
A=M
D=M
@addr
A=M
M=D

//push constant 123
@123
D=A
@SP
A=M
M=D
@SP
M=M+1
@7777
M=1
M=0

//call Sys.add12 1
@RETURN_ADDR_2
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@6
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@7777
M=1
M=0
@Sys.add12
0;JMP
(RETURN_ADDR_2)

//pop temp 0
@SP
M=M-1
@SP
A=M
D=M
@R5
M=D

//push local 0
@LCL
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1

//push local 1
@LCL
D=M
@1
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1

//push local 2
@LCL
D=M
@2
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1

//push local 3
@LCL
D=M
@3
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1

//push local 4
@LCL
D=M
@4
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1

//add
@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1

//add
@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1

//add
@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1

//add
@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1
@7777
M=1
M=0

//return
@LCL
D=M
@frame
M=D
@frame
D=M
@5
A=D-A
D=M
@retAddr
M=D
@SP
M=M-1
@SP
A=M
D=M
@ARG
A=M
M=D
@ARG
D=M
@SP
M=D
@SP
M=M+1
@frame
D=M
@1
A=D-A
D=M
@THAT
M=D
@frame
D=M
@2
A=D-A
D=M
@THIS
M=D
@frame
D=M
@3
A=D-A
D=M
@ARG
M=D
@frame
D=M
@4
A=D-A
D=M
@LCL
M=D
@7777
M=1
M=0
@retAddr
A=M
0;JMP

//function Sys.add12 0
(Sys.add12)
D=0

//push constant 4002
@4002
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop pointer 0
@SP
M=M-1
@SP
A=M
D=M
@THIS
M=D

//push constant 5002
@5002
D=A
@SP
A=M
M=D
@SP
M=M+1

//pop pointer 1
@SP
M=M-1
@SP
A=M
D=M
@THAT
M=D

//push argument 0
@ARG
D=M
@0
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1

//push constant 12
@12
D=A
@SP
A=M
M=D
@SP
M=M+1

//add
@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1
@7777
M=1
M=0

//return
@LCL
D=M
@frame
M=D
@frame
D=M
@5
A=D-A
D=M
@retAddr
M=D
@SP
M=M-1
@SP
A=M
D=M
@ARG
A=M
M=D
@ARG
D=M
@SP
M=D
@SP
M=M+1
@frame
D=M
@1
A=D-A
D=M
@THAT
M=D
@frame
D=M
@2
A=D-A
D=M
@THIS
M=D
@frame
D=M
@3
A=D-A
D=M
@ARG
M=D
@frame
D=M
@4
A=D-A
D=M
@LCL
M=D
@7777
M=1
M=0
@retAddr
A=M
0;JMP

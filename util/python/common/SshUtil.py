#!/usr/bin/python3

import paramiko

################################################
# class SshCommand
################################################

class SshCommand(object):
    def __init__(self, argCmdLine, argClient):

        self.cmdLine = argCmdLine
        self.sshClient = argClient
        self.stdIn = None
        self.stdOut = None
        self.stdErr = None
        self.status = None

################################################
# getConnection
################################################

def getSshClient(argConfig, argSecFile, argLogger):

    try:

        key = paramiko.RSAKey.from_private_key_file(argSecFile)
        result = paramiko.SSHClient()
        result.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        #print("connecting")
        result.connect(hostname=argConfig.host, username=argConfig.user, pkey=key)
        #print("connected")

        #transport = result.get_transport()
        #sftp = paramiko.SFTPClient.from_transport(transport)


    except:

        result = None

    logResult = True if result is not None else False

    argConfig.dumpPropertyLog(logResult, argLogger)

    return result

################################################
# sftpFile
################################################

def sftpPutFile(argSshClient, argSrc, argTrg):
    transport = argSshClient.get_transport()
    sftp = paramiko.SFTPClient.from_transport(transport)
    sftp.put(argSrc, argTrg)
    sftp.close()

################################################
# sshExec
################################################

def sshExec(argSshClient, argDir, argCmd):

    retCode = 0
    retMsg = ""

    cdCmd = "cd %s" % argDir

    paramikoCmd = "%s; %s" % (cdCmd, argCmd)

    stdin_, stdout_, stderr_ = argSshClient.exec_command(paramikoCmd)
    # time.sleep(2)    # Previously, I had to sleep for some time.
    stdout_.channel.recv_exit_status()

    stdErrLines = stderr_.readlines()

    for stdErrLine in stdErrLines:
        retCode = 1
        #print(stdErrLine)
        retMsg += stdErrLine

    if retCode == 0:
        #return retCode, retMsg

        stdOutLines = stdout_.readlines()
        for stdOutLine in stdOutLines:
            retMsg += stdOutLine
            #print(stdOutLine)

    retMsg = retMsg.strip()

    return retCode, retMsg


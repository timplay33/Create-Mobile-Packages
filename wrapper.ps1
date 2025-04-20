<#
.SYNOPSIS
  Git-Flow Wrapper for PowerShell (Windows)
  Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
  
  usage:
  ..\wrapper.ps1 1.21.1 hotfix start "#issue"
#>

param(
  [Parameter(Mandatory=$true)][string]$Version,
  [Parameter(ValueFromRemainingArguments=$true)][string[]]$FlowArgs
)

# set git flow config
git config gitflow.branch.master "mc$Version/main"
git config gitflow.branch.develop "mc$Version/dev"
git config gitflow.prefix.feature "mc$Version/feature/"
git config gitflow.prefix.hotfix   "mc$Version/hotfix/"
git config gitflow.prefix.release  "mc$Version/release/"
git config gitflow.prefix.support  "mc$Version/support/"

# run git flow
git flow @FlowArgs

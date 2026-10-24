package service

import "errors"

var (
	ErrInvalidArgument = errors.New("invalid argument")
	ErrPolicyNotFound  = errors.New("policy not found")
	ErrInvalidPolicy   = errors.New("invalid policy configuration")
)

# T-002 — Domain model warm-up

**Module:** `ledger-core` · **Closes:** nothing critical on its own — this is a 15-minute warm-up
before T-003, to get comfortable in the codebase before the actual concurrency exercise.

## What's already there

`Money` (immutable, `BigDecimal`-backed, scale-2) and `Account` (id, balance, an unused
`ReentrantLock`) are both complete. `LedgerTransferService.InsufficientFundsException` exists.

## What to add

Pick 2–3 of these — enough to feel comfortable with the types before T-003, not an exhaustive list:

1. `Money` currently throws `ArithmeticException` (via `RoundingMode.UNNECESSARY`) if constructed
   from a value with more than 2 decimal places. Decide if that's the right behaviour for this
   domain (it probably is, for a ledger — money shouldn't silently lose precision) and write a test
   asserting it.
2. `Account`'s constructor accepts a negative `openingBalance` today. Decide whether that should be
   allowed (an overdraft-permitting account is a legitimate design; a ledger bug is not) and, if
   not, add validation + a test.
3. Add `Money.isPositive()` / a cleaner comparison API if `isLessThan`/`isNegative` feels
   insufficient once you're writing T-003's test.

## Definition of done

`MoneyTest` (and a new `AccountTest` if you add one) pass, and you've made and can explain one
deliberate design decision about validation — that's worth more here than the code itself.

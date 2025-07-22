# CoffeeRunner
A toy problem to decide who is paying for coffee

## Authors
Zach Hess (owner)

## Description

At a high level, the goal is to determine "fairly" who is paying for coffee every afternoon. Barring any further requirements, the decision tree for fairness is left to the discretion of the engineer (who has pinky-promised not to write anything to unfairly advantage themselves into the solution.)
Here are the understood constraints and assumptions:

### Constraints

1. Only one person pays for the afternoon coffee run each day.

### Assumptions

1. The team is flexible. While it _currently_ numbers 7 individuals (Bob, Jim, and 5 others), more regulars may attend, and existing ones may leave.
1. Individuals have a favored drink, but _may_ change their order on any given day.
   1. Prices of drinks at the coffee shop vary significantly - and this needs to be accounted for in the "fairness" decision logic. A simple round-robin wouldn't be fair if Jim's $2 black coffee is weighted the same as Bob's $6 cappuccino.
1. Any abuse of the algorithm will be met with the wrath of the team.

## Solution

A "weighted round-robin" solution is deemed most-fair. For time constraints, a CLI solution will be implemented first, but will be left extensible so that an API can be implemented later.

### The Decision Algorithm

In the following example, if Joe's daily drink (a black coffee) costs $2 and Bob's drink (a cappuccino) costs $6, it is most "fair" if Bob pays for drinks roughly 3 times as frequently as Jim. The algorithm should also account for how long a teammate has gone since they last paid for drinks, or,
more precisely, how much coffee-value have they received since they last paid. A weighted-round robin algorithm accounts for this. Consider the following example:

#### Example

1. Yesterday, Jim paid for coffee, so his weight would be **0** (no money-value received since he last paid)
1. Bob had 2 cappucinos since he last paid, so his weight is **12** (2 * $6)
1. For simplicity, the rest of the team (Greg, Garg, Gorg, and Grog) all drink Mochas, which cost $4.50, respectively, they have gone 1, 3, 4, 5, and 6 days since they last paid, making their weights **4.5, 13.5, 18, 22.5, and 27**, respectively
1. Everyone is ordering their "usual" today, so first thing, we increment everyone's weight by the value of their order:
   1. Jim: **2**
   1. Bob: **18**
   1. Greg: **9**
   1. Garg: **18**
   1. Gorg: **27**
   1. Grog: **31.5**
1. The person with the highest weight pays for the order. In this case it's **Grog (31.5)**. Since he paid today, his weight is reset to 0. Everyone else's new weight is preserved and, if tomorrow everyone orders their usual again, it will be **Gorg's** turn to pay.
1. In case of two teammate's weights being equal, one is selected randomly to pay and their weight is reset.

Over time, Bob's weight grows much faster than everyone else's so he will pay more frequently. New teammates joining the rotation can have their weights start at 0, since we're incrementing weights _before_ deciding who pays for an order. Also teammates that don't join
regularly will not have to pay as frequently since their weights do not increase as often.

### Implementation

In the interest of time, a CLI-based solution has been created, printing everyone's order to console along with a total price that can be sent via email to the buyer's email (makes putting in the order easier). A REST endpoint and responsive app are also an option for future work. For now,
I have assumed that the amount of data we're working with can be stored in memory, so I have opted to just serialize application state and history to a file; database interaction is also a future design consideration.
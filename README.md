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

A "weighted round-robin" solution is deemed the most-fair. For time constraints, a CLI solution will be implemented first, but will be left extensible so that an API can be implemented later.

### The Decision Algorithm

In the following example, if Joe's daily drink (a black coffee) costs $2 and Bob's drink (a cappuccino) costs $6, it is most "fair" if Bob pays for drinks roughly 3 times as frequently as Jim. The algorithm should also account for how long a teammate has gone since they last paid for drinks, or,
more precisely, how much coffee-value have they received since they last paid. A weighted-round robin algorithm accounts for this. Consider the following example:

#### Example

1. Yesterday, Jim paid for coffee, so his weight would be **0** (no money-value received since he last paid)
1. Bob had 2 cappucinos since he last paid, so his weight is **12** (2 * $6)
1. For simplicity, the rest of the team all drinks mochas (costing $4.50) of some variety, and it's been **1, 3, 4, 5, and 6** days since they paid, for weights of **4.5, 13.5, 18, 22.5, and 27**, respectively.
1. We can represent these weights as consecutive numeric ranges for all members with non-zero weights:
   1. Bob's range: **0 - 12**
   1. Teammate #1's range: **>12 - 16.5**
   1. Teammate #2's range: **>16.5 - 30**
   1. Teammate #3's range: **>30 - 48**
   1. Teammate #4's range: **>48 - 70.5**
   1. Teammate #5's range: **>70.5 - 97.5**
1. Now we can "roll" a number across the entire weighted range (0 - 97.5). The likelihood of any teammate's range being hit is weighted by how much value they have received since they last paid. In this case, we'll say the random roll comes back **8.0**.
1. Bob pays for coffee today, with every teammate getting their regular.
1. Bob's weight is reset to 0 since he paid, he will definitely not be buying tomorrow.
1. Everyone else's range is incremented by the value of their order; so Jim's weight is now **2** and all the other teammate's weights increase by **4.5**

Over time, people who order more expensive drinks should end up buying more frequently, but no one buys two consecutive days in a row. To solve the "who buys first" problem, we can arbitrarily set everyone's initial weight to 1 (or their favored drink value). Newcomers after the rotation starts
can have their initial weight set to the **current average weight** so that they don't automatically get free drinks for an absurd length of time.

#### Potential Issues

The proposed solution is more like "coffee roulette" in that it's fair "over time" as purchases average out, but a run of bad luck may mean that someone ordering a cheap drink may end up paying every other day if the RNG deities frown upon them.

### Implementation

In the interest of time, a CLI-based solution has been created, printing everyone's order to console along with a total price that can be sent via email to the buyer's email (makes putting in the order easier). A REST endpoint and responsive app are also an option for future work. For now,
I have assumed that the amount of data we're working with can be stored in memory, so I have opted to just serialize application state and history to a file; database interaction is also a future design consideration.
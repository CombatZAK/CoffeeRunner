# CoffeeRunner
A toy problem to decide who is paying for coffee

## Authors
Zach Hess (owner)

## Description

At a high level, the goal is to determine "fairly" who is paying for coffee every afternoon. Barring any further requirements, the decision tree for fairness is left to the discretion of the engineer (who has pinky-promised not to write anything to unfairly advantage themselves into the solution.)
Here are the understood constraints and assumptions:

### Constraints

1. Only one person pays for the afternoon coffee run each day.
1. We must account for drink price when ensuring fairness in drink purchases.

### Assumptions

1. The team is flexible. While it _currently_ numbers 7 individuals (Bob, Jim, and 5 others), more regulars may attend, and existing ones may leave.
1. Individuals have a favored drink, but _may_ change their order on any given day.
   1. Prices of drinks at the coffee shop vary significantly - and this needs to be accounted for in the "fairness" decision logic. A simple round-robin wouldn't be fair if Jim's $2 black coffee is weighted the same as Bob's $6 cappuccino.
1. Teammates have unique names, and we can use unique "friendly names" as UIDs for this exercise.
1. Only someone who is getting coffee on any given day is elligible to pay; solo orders will not affect weights for future orders.
1. Only one coffee run happens per day.
1. Any teammate finding an exploit in the application is due a free coffee bug bounty from the author.

## Solution

A "weighted round-robin" solution is should be adequate so resolve the fairness concern. For time constraints, a CLI solution will be implemented first, but will be left extensible so that an API can be implemented later.z

### The Decision Algorithm

In the following example, if Joe's daily drink (a black coffee) costs $2 and Bob's drink (a cappuccino) costs $6, it is most "fair" if Bob pays for drinks roughly 3 times as frequently as Jim. The algorithm should also account for how long a teammate has gone since they last paid for drinks, or,
more precisely, how much coffee-value have they received since they last paid. A weighted-round robin algorithm accounts for this. Consider the following example:

#### Example

1. Yesterday, Jim paid for coffee, so his weight would be **0** (no money-value received since he last paid)
1. Bob had 2 cappucinos since he last paid, so his weight is **12** (2 * $6)
1. For simplicity, the rest of the team (Greg, Garg, Gorg, Grog) all drink Mochas, which cost $4.50; respectively, they have gone 1, 3, 4, 5, and 6 days since they last paid, making their weights **4.5, 13.5, 18, 22.5, and 27**, respectively
1. Everyone is ordering their "usual" today, so first thing, we increment everyone's weight by the value of their order:
   1. Jim: **2**
   1. Bob: **18**
   1. Greg: **9**
   1. Garg: **18**
   1. Gorg: **22.5**
   1. Grog: **27**
   1. George: **31.5**
1. The person with the highest weight pays for the order. In this case it's **George (31.5)**. Since he paid today, his weight is reset to 0. Everyone else's new weight is preserved and, if tomorrow everyone orders their usual again, it will be **Grog's** turn to pay.
1. In case of two teammate's weights being equal, the one who went the longest duration without purchasing will be selected.
   1. there is an edge case when the team is first initialized where last-purchase date and weights are equal. In this event, a teammate will be chosen arbitrarily (whoever appears first in the sort).

Over time, Bob's weight grows faster than everyone else's so he will pay more frequently. New teammates joining the rotation can have their weights start at 0, since we're incrementing weights _before_ deciding who pays for an order. Also teammates that don't join
regularly will not have to pay as frequently since their weights do not increase as often.

### Implementation

In the interest of time, a CLI-based solution has been created, printing everyone's order to console along with a total price that can be sent via email to the buyer's email (makes putting in the order easier). A REST endpoint and responsive app are also an option for future work. For now,
I have assumed that the amount of data we're working with can be stored in memory, so I have opted to just serialize application state and history to a file; database interaction is also a future design consideration.

## Setup

CoffeeRunner is a Java project using Gradle to manage builds and tests. The following commands are used to build the project.

### Build/Package

1. From repository root, run: `gradlew [clean] build jar`; this generates the executable jar file at `build/libs/`; current version is `CoffeeRunner-0.1.jar`.
1. Optionally, you can move this file to a directory of your choice for execution; an installer is currently not provided and there are no configuration settings due to the simplicity of the application.

### Execution

The team data is stored in a single JSON file in the same directory as the JAR file. The name of this file is `team.json`. The first thing you will need to do is create this file - you can do so manually referencing the [team schema](#team-schema) or you can initialize it using the `INIT` command
shown here:

```
java -jar CoffeeRunner-0.1.jar INIT [JSON_ARRAY]
```

Where `JSON_ARRAY` is an optional argument that uses the same [team schema](#team-schema). Following intitialization, you can add or modify teammates, or start running the ordering operation.

#### PUT command

**PUT** adds a new teammate to the team file, or updates an existing one based on a unique name match. This command accepts a single JSON argument, the user to be added or updated with all editable fields as you want them to appear. See the [teammate schema](#teammate-schema) for more information on
constructing this argument.

Example:

```
java -jar CoffeeRunner-0.1.jar PUT '{"name": "Jim", "regular_order": { "name": "Coffee 16oz", "drink_options": "half and half", "price": 2.0 }, "is_active": true }'
```

This command will add a new teammate, `Jim` to the team, or modify the fields of the entry with that name if it already exists.

#### ORDER command

**ORDER** Creates a new coffee order for the day, determines who should pay, and updates the weights of each teammate accordingly. The command can be run without any arguments, or can accept a JSON argument indicating who is going to get coffee, and any non-standard drink orders. See the [order schema](#order-schema)
for more information.

Examples:

The following command issues an order for all **active** teammmates using their regular drink orders and updates the `teams.json` file.

```
java -jar CoffeeRunner-0.1.jar ORDER
```

The following command issues an order **only** for teammates **Jim**, **Bob**, and **Greg** and updates their entries in `teams.json` accordingly; no other teammates are modified. These teammates **must** exist prior to running this command. Note that null values indicate these teammates
are receiving their regular drink orders.

```
java -jar CoffeeRunner-0.1.jar ORDER '{"Jim": null, "Bob": null, "Greg": null}'
```

The following command issues an order for teammates **Jim**, **Bob** and **Greg** as above, but overrides **Jim**'s order with custom drink.

```
java -jar CoffeeRunner-0.1.jar ORDER '{"Jim": {"name": "Mocha", "drink_options": null, "price": 4.5}, "Bob": null, "Greg": null}'
```

## Argument Schema

Inputs for each operation of CoffeeRunner are provided in JSON format. Schema for each object and allowed values are provided here.

### Team Schema

The team schema is a **JSON array** tracking the state of each teammate. Each element of the array must be a **non-null** teammate object (see [teammate-schema]) for more information. The `team.json` file follows this schema. 

#### Teamm Example

This JSON block shows a team with two teammates.

```json
[
   {
      "name": "Jim",
      "regular_order": {
         "name": "Coffee 16oz",
         "drink_options": null,
         "price": 2.0
      },
      "is_active": true,
      "weight": 2.0,
      "last_purchase": "2025-07-20"
   },
   {
      "name": "Bob",
      "regular_order": {
         "name": "Cappuccino",
         "drink_options": "2% milk",
         "price": 6.0
      },
      "is_active": true,
      "weight": 0.0,
      "last_purchase": "2025-07-21"
   }
]
```

> [!IMPORTANT]
> Teammate names **must** be unique.

### Teammate Schema

A **teammate** is a **JSON object** tracking the state of an individual teammate. This object is stored in the team array (see [team schema](#team-schema)), and is also used to add or update teammates with the **PUT** command. A description of each field of this object follows:

| Field | Type | Required | Used in PUT | Description |
|-------|------|----------|-------------|:------------|
| **name** | String | **YES** | **YES** | Unique identifier for teammate |
| **regular_order** | [drinkOrder](#drinkorder-schema) | **YES** | **YES** | Teammate's "default" drink order |
| **is_active** | boolean | **NO** | **NO** | True if the teammate is part of the "default" order list (default `true`) |
| **weight** | Number | **NO** | **NO** | The running sum of drink the teammate has received for "free" since they last paid (default 0) |
| **last_purchase** | String | **NO** | **NO** | The date of the teammate's last turn purchasing coffee in the format `yyyy-MM-dd` | 

#### Teammate Example

```json
{
   "name": "Greg",
   "regular_order": {
      "name": "Mocha",
      "drink_options": null,
      "price": 4.5
   },
   "is_active": true,
   "weight": 13.5,
   "last_purchase": "2025-07-19"
}
```

### DrinkOrder Schema

A **drinkOrder** is a **JSON object** tracking a specific coffee drink, either as a teammate's favored drink or as a variation on the day's order. A description of each field follows:

| Field | Type | Required | Description |
|-------|------|----------|:------------|
| **name** | String | **YES** | Name of drink to be ordered |
| **drink_options** | String | **NO** | Any extra modifications to the order |
| **price** | Number | **YES** | Total purchase price of the drink |

#### DrinkOrder Example

```json
{
   "name": "Frapuccino",
   "options": "Extra sprinkles",
   "Price": 8.5
}
```

### Order Schema

An **order** is a **JSON object** (dictionary) using teammate names as keys, with optional values representing non-default orders for the day. It is used to create the day's coffee run and update the running totals for the team. The label for each member of the object
are the unique teammate names of the teammates participating in the order. The values assigned to each label are either `null` or a [drinkOrder object](#drinkorder-schema).

#### Order Example

```json
{
   "Jim": null,
   "Bob": {
      "name": "Coffee 20oz",
      "options": null,
      "price": 2.5
   },
   "Greg": null
}
```

## Further Improvements

This solution was developed quickly (~1 day worth of work), with expedience and simplicity being the driver for most design decisions. In terms of usability, the CLI solution is fraught with inconveniences, requiring the user to form JSON manually, though the "default" options minimize this. The
requirement to develop this solution as an application restricts what may be a more usable alternative -- a scripted spreadsheet or a Teams chatbot backed by LogicApps/PowerAutomate. The latter alternative is very promising, and prior experience tells me that a LogicApp chat-bot may be an ideally
usable solution to this problem - albeit one that does not demonstrate any of my coding ability.
# Prudence

A personal, single-user Android app for tracking expenses: recording what was spent, when, and on what, and reporting over arbitrary time intervals.

## Language

**Expense**:
A single recorded act of spending: an amount, a date (day granularity), exactly one category, and an optional note.
_Avoid_: payment, transaction, purchase

**Category**:
A node in a single user-editable tree of arbitrary depth that classifies expenses. Every expense belongs to exactly one category; reports roll child categories up into their ancestors. Deleting a category reassigns its expenses and child categories to its parent; moving a category (with its subtree) is always allowed, and past reports follow the current tree.

**Uncategorized**:
The one permanent root category, which cannot be deleted. Deleting a root category sends its expenses and child categories here.

**Report**:
A computed view over an interval: the total spent, and a breakdown by category with hierarchy rollup. Drill-down the tree one level at a time; categories with zero spending in the interval are hidden.

**Interval**:
The date range a report covers: a preset (this week — Monday to Sunday, this month — calendar month) or a custom inclusive start/end pair.
_Avoid_: period, time span

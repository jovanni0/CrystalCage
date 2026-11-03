---
title: The app
description: A walkthrough of Sanctum, from the library to logging a read.
nav_label: The app
order: 2
---

## Getting started

The first time you open Sanctum, it looks for a server on your network. Pick yours from the list, or enter its address manually (see [Connecting the app](/docs/hosting#connecting-the-app)). Then log in, or create an account with an :term[invite code]{key="invite-code"} from the server's admin.

## The library

![The library, a grid of book covers.](/images/library.png)

The library shows all your books as a grid of covers, each with its title. Tap a book to open its profile.

## Adding a book

![The add book form.](/images/add-book.png)

A book is described by:

- **identity**: author, title, and optionally a :term[universe]{key="universe"}, a :term[series]{key="series"} and its volume number
- **details**: release date and word count
- **tags**, grouped into categories
- **blurb**: the book's description
- **thoughts**: your own opinion of the book

The blurb and your thoughts support Markdown, so you can use headings, lists and emphasis.

Covers and ebook files are not added in this form. Once the book exists, you add them from its profile.

## The book profile

![A book profile on the Book tab.](/images/book.png)

Everything about a book is on its profile, split into four tabs: **Covers**, **Book**, **Reads** and **Editions**. Swipe sideways to move between them. The button in the top bar changes with the tab, so it always does the main action for what you are looking at.

### Covers

![The Covers tab with several covers.](/images/covers.png)

A book can have several covers, for example from different editions or printings. Pick the one the library shows. Tap a cover to open it full screen: pinch or double tap to zoom, swipe sideways to see the next cover, and swipe down to close it.

### Book

The **Book** tab shows the book's details, tags, blurb and your thoughts, along with a summary of your reads: how many times you have finished it, or whether you did not finish it.

### Editions

![The Editions tab with an EPUB and a PDF edition.](/images/editions.png)

An :term[edition]{key="edition"} is one of your ebook files for the book. Upload as many as you have, in any format. EPUB, MOBI and PDF are color-coded; other formats are kept as they are.

:::warning
Deleting an edition also deletes the reads logged on it. The app tells you how many before you confirm.
:::

## Tracking reads

![The Reads tab with a read in progress.](/images/reads.png)

A read is one time you read the book. Start a read on one of the book's editions, then update your progress as you go. Progress is saved as a percentage on the server; page numbers depend on your device and font size, so they are kept only on your phone.

While a read is in progress, the book profile shows it in a banner at the top. When you are done, mark the read as finished, or as :term[did not finish]{key="dnf"} if you stopped.

Reads you finished before using Sanctum can be logged as :term[historical reads]{key="historical-read"}, with the dates you remember. Every finished read counts, so rereads show up in the book's summary.

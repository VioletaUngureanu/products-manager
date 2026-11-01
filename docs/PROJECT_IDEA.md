
# CosmeticManager: Project Idea

#### 1. A short description
CosmeticManager is an application for visualizing and managing your cosmetics products. The app is meant to be make your life easier, by helping you keep track of the cosmetics you buy, their expiration dates, and your buget. You can use it to add products as you buy them, to view them all, to edit them and to delete them. The app will also help you keep track of your opened products and of their expiration dates. You don't need a 5 in 1 to keep your cosmetics decluttered. You just need CosmeticManager, a bit of time and no stress.

#### 2. Domain details
The app stores one kind of item, a CosmeticProduct.
Cosmetic product (external fields):
- name : required
- description : not required
- category : required
- bought date : not required
- opened date : not required
- shelf life : required
- ingredients : not required

Cosmetic product (internal fields):
- id
- last modified date
- sent to the server

#### 3. CRUD

Create

    The user can add a new product.
    The user must provide a name, category, shelf life. The description, bought date, opened date and ingredients are optional and can be left empty.

Read

    The user can see:
    - all your products;
    - only the unopened products;
    - only the opened products;
    - only the products that are about to expire. A product counts as about to expire when it was opened and its shelf life is almost over.
    The user can also open a single product to see all its details.

Update

    The user can change a product's name, description, category, shelf life and ingredients. The user can also set the opened date if it was empty, but after that it cannot be changed, so the app can reliably calculate how long an opened product stays safe to use.

Delete

    The user can delete any product, for example when you have finished it or thrown it away.

#### 4. Persistence details

The products are saved in two places: on the phone, so the app is fast and works without internet, and on the server, so the products list is safe (in case the user loses or changes the phone).

Create

    it is saved on the phone, when online sent to the server and saved there too
Read

    always shows the products from the phone storage, when online the app refreshes the list from the server
Update

    it is saved on the phone, when online sent to the server and saved there too
Delete

    it is saved on the phone, when online sent to the server and saved there too

#### 5. Offline scenarios

Create

    The user buys a new shampoo and adds it with no internet. It appears in their list right away, with a small "waiting to sync" icon. When the internet comes back, the app sends it to the server on its own and the icon disappears.

Read

    The user opens the app on the train without internet. The user still see all their products and can switch between all, unopened, opened and about to expire. A small message tells them that they're offline and the list may not include changes made from another device.

Update

    The user marks a moisturizer as opened while offline. The change is saved on their phone immediately, and the product shows the "waiting to sync" icon. Once they're back online, the change is sent to the server. The rule that an opened date can't be changed once set applies offline too. If the same product was changed on another device in the meantime, the most recent change is kept.

Delete

    The user deletes a lipstick with no internet. It disappears from their list immediately. The app remembers that it still has to tell the server, and does so when the internet returns. If the app is closed in the meantime, the deletion is not lost.

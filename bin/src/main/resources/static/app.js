document.addEventListener('DOMContentLoaded', () => {
  // Original code to disable manual scroll on New Arrivals container.
  // Commenting this out to allow manual scrolling on New Arrivals as well.
  // const newArrivalsContainer = document.querySelector('#home > .scrolling-wrapper-auto');
  // if (newArrivalsContainer) {
  //   newArrivalsContainer.addEventListener('wheel', (e) => {
  //     e.preventDefault();
  //   });
  // }

  // Search bar filtering for all books (New Arrivals and Genre sections)
  const searchInput = document.getElementById('searchBar');
  // Select all book cards, regardless of whether they are in New Arrivals or Genre sections
  const allBookCards = document.querySelectorAll('.book-card, .book-genre-card');

  if (searchInput) {
    searchInput.addEventListener('input', () => {
      const filter = searchInput.value.toLowerCase();

      allBookCards.forEach(card => {
        const titleElement = card.querySelector('h6');
        const authorElement = card.querySelector('p');
        
        const title = titleElement ? titleElement.textContent.toLowerCase() : '';
        const author = authorElement ? authorElement.textContent.toLowerCase() : '';
        
        // For genre cards, the "genre" isn't explicitly tagged, but the section title acts as a genre.
        // We can't directly search by "genre" unless it's explicitly in the card's data.
        // However, searching by title and author should cover most user search intentions.
        // If you want to search by genre, you would need to add a data-genre attribute to your book cards.

        if (title.includes(filter) || author.includes(filter)) {
          card.style.display = ''; // show
        } else {
          card.style.display = 'none'; // hide
        }
      });
    });
  }
});
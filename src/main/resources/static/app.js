document.addEventListener('DOMContentLoaded', () => {
  // Define the base URL for your Spring Boot backend API
  const API_BASE_URL = 'http://localhost:8080/api/books';

  const newArrivalsContainer = document.getElementById('newArrivalsContainer');
  const fictionGenreContainer = document.getElementById('fictionGenreContainer');
  const nonFictionGenreContainer = document.getElementById('nonFictionGenreContainer');
  const scienceGenreContainer = document.getElementById('scienceGenreContainer');
  const educationGenreContainer = document.getElementById('educationGenreContainer');
  const businessGenreContainer = document.getElementById('businessGenreContainer');
  const searchInput = document.getElementById('searchBar');

  // Function to create a single book card HTML element
  function createBookCard(book) {
    const availabilityStatus = book.availableCopies > 0 ? 'Available' : 'Unavailable';
    const badgeClass = book.availableCopies > 0 ? 'bg-success' : 'bg-danger';

    // Ensure the card has data attributes for filtering by title, author, and genre
    return `
      <div class="book-card" 
           data-title="${book.title ? book.title.toLowerCase() : ''}" 
           data-author="${book.author ? book.author.toLowerCase() : ''}" 
           data-genre="${book.genre ? book.genre.toLowerCase() : ''}">
        <div class="book-info">
          <h6>${book.title}</h6>
          <p>${book.author}</p>
          <span class="badge ${badgeClass}">${availabilityStatus}</span>
        </div>
      </div>
    `;
  }

  // Function to display books in a given container
  function displayBooksInContainer(books, containerElement) {
    if (containerElement) {
      containerElement.innerHTML = ''; // Clear existing content
      books.forEach(book => {
        containerElement.innerHTML += createBookCard(book);
      });
    }
  }

  // Function to fetch and display books from the backend
  async function fetchAndDisplayBooks() {
    try {
      const response = await fetch(API_BASE_URL);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const books = await response.json();

      // Clear existing content in all containers before re-populating
      newArrivalsContainer.innerHTML = '';
      fictionGenreContainer.innerHTML = '';
      nonFictionGenreContainer.innerHTML = '';
      scienceGenreContainer.innerHTML = '';
      educationGenreContainer.innerHTML = '';
      businessGenreContainer.innerHTML = '';

      // Separate books by genre for display
      const booksByGenre = {
        'fiction': [],
        'non-fiction': [],
        'science': [],
        'education': [],
        'business': []
      };

      // Populate books into their respective genre categories
      books.forEach(book => {
        const genre = book.genre ? book.genre.toLowerCase() : 'unknown';
        if (booksByGenre[genre]) {
          booksByGenre[genre].push(book);
        } else {
          // If a genre from the backend doesn't match a predefined category,
          // you might want to add it to a 'Miscellaneous' category or similar.
          // For now, it will just not be displayed in a specific genre section.
        }
      });

      // Display "New Arrivals" (e.g., the first 6 books fetched, or based on a 'yearPublished' field if available)
      // For this example, we'll just take the first 6 books from the fetched list.
      const newArrivals = books.slice(0, 6);
      displayBooksInContainer(newArrivals, newArrivalsContainer);

      // Display books in their respective genre sections
      displayBooksInContainer(booksByGenre['fiction'], fictionGenreContainer);
      displayBooksInContainer(booksByGenre['non-fiction'], nonFictionGenreContainer);
      displayBooksInContainer(booksByGenre['science'], scienceGenreContainer);
      displayBooksInContainer(booksByGenre['education'], educationGenreContainer);
      displayBooksInContainer(booksByGenre['business'], businessGenreContainer);

    } catch (error) {
      console.error('Error fetching and displaying books:', error);
      // Display a user-friendly message if fetching fails
      newArrivalsContainer.innerHTML = '<p class="text-danger">Failed to load books. Please ensure the backend is running.</p>';
    }
  }

  // Search bar filtering for all books
  if (searchInput) {
    searchInput.addEventListener('input', () => {
      const filter = searchInput.value.toLowerCase();
      // Select all book cards across all sections
      const allBookCards = document.querySelectorAll('.book-card');

      allBookCards.forEach(card => {
        // Get data from data attributes for robust filtering
        const title = card.dataset.title || '';
        const author = card.dataset.author || '';
        const genre = card.dataset.genre || '';

        if (title.includes(filter) || author.includes(filter) || genre.includes(filter)) {
          card.style.display = ''; // show the card
        } else {
          card.style.display = 'none'; // hide the card
        }
      });
    });
  }

  // Initial fetch and display when the DOM is loaded
  fetchAndDisplayBooks();
});

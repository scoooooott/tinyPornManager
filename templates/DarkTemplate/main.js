window.addEventListener('load', () => {

  // lazy loading of images

  let images = [...document.querySelectorAll('[loading=lazy]')]

  const interactSettings = {
    root: null,
    rootMargin: '0px 0px 200px 0px'
  }

  function onIntersection(imageEntites) {
    imageEntites.forEach(image => {
      if (image.isIntersecting) {
        observer.unobserve(image.target)
        image.target.src = image.target.dataset.src
      }
    })
  }

  let observer = new IntersectionObserver(onIntersection, interactSettings)

  images.forEach(image => observer.observe(image))

  // title search 
  
  const minMatchScore = 0.1
  const movieBlocks = document.getElementsByClassName('movie')
  const input = document.getElementById('movietitle')
  const titles = []
  const idByTitle = {}
  let searchSet
  
  for (let m of movieBlocks) {
    const title = m.getAttribute('data-title').normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    const id = m.getAttribute('data-id')
    titles.push(title)
    idByTitle[title] = id
  }
  searchSet = FuzzySet(titles, false, 2, 3)
  
  input.addEventListener('keyup', () => {
    const value = input.value.trim()
          .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    
    const isSearchMode = value.length > 2 
    // set visibility of original movies
   toggleSearchMode(isSearchMode)
    // launch search
    if (isSearchMode) {
      const a = searchSet.get(value, null, minMatchScore)
      if (a != null) {
        // we only keep the 
        const maxScore = a[0][0]
        const answers = a
              .filter(a => (a[0] - minMatchScore) / (maxScore - minMatchScore) > 0.5)
              .map(v => v[1])
        displayAnswers(answers)
      }
    }
  })
  
  let displayAnswers = function(titleAnswers) {
    const answers = document.querySelector('#answers')
    //empty the answers 
    while(answers.children[0]) {
      answers.removeChild(answers.children[0])
    }
    // insert new answers
    for (let title of titleAnswers) {
      const id = idByTitle[title]
      const m = document.getElementById(id).cloneNode(true)
      delete m.id
      // load lazy images
      let lazyImages = m.querySelectorAll('[loading=lazy]')
      lazyImages.forEach(image => {
        image.src = image.dataset.src
      })
      
      answers.appendChild(m)
    }
  }

  let toggleSearchMode = function(actived) {
    const answers = document.querySelector('#answers')
    const movies = document.querySelector('#movies')

    if (actived) {
      answers.style.removeProperty('display')
      movies.style.display = 'none'
    } else {
      answers.style.display = 'none'
      movies.style.removeProperty('display')
    }
  }

})

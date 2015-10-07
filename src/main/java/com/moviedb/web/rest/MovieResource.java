package com.moviedb.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.moviedb.domain.Movie;
import com.moviedb.repository.MovieRepository;
import com.moviedb.web.rest.util.HeaderUtil;
import com.moviedb.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Movie.
 */
@RestController
@RequestMapping("/api")
public class MovieResource {

    private final Logger log = LoggerFactory.getLogger(MovieResource.class);

    @Inject
    private MovieRepository movieRepository;

    /**
     * POST  /movies -> Create a new movie.
     */
    @RequestMapping(value = "/movies",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) throws URISyntaxException {
        log.debug("REST request to save Movie : {}", movie);
        if (movie.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new movie cannot already have an ID").body(null);
        }
        Movie result = movieRepository.save(movie);
        return ResponseEntity.created(new URI("/api/movies/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("movie", result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /movies -> Updates an existing movie.
     */
    @RequestMapping(value = "/movies",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Movie> updateMovie(@RequestBody Movie movie) throws URISyntaxException {
        log.debug("REST request to update Movie : {}", movie);
        if (movie.getId() == null) {
            return createMovie(movie);
        }
        Movie result = movieRepository.save(movie);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("movie", movie.getId().toString()))
                .body(result);
    }

    /**
     * GET  /movies -> get all the movies.
     */
    @RequestMapping(value = "/movies",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Movie>> getAllMovies(Pageable pageable)
        throws URISyntaxException {
        Page<Movie> page = movieRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/movies");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /movies/:id -> get the "id" movie.
     */
    @RequestMapping(value = "/movies/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        log.debug("REST request to get Movie : {}", id);
        return Optional.ofNullable(movieRepository.findOne(id))
            .map(movie -> new ResponseEntity<>(
                movie,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /movies/:id -> delete the "id" movie.
     */
    @RequestMapping(value = "/movies/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        log.debug("REST request to delete Movie : {}", id);
        movieRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("movie", id.toString())).build();
    }
}

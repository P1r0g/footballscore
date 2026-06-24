package edu.rutmiit.demo.demorest.assemblers;

import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.demorest.controllers.TeamController;
import edu.rutmiit.demo.demorest.controllers.MatchController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MatchModelAssembler implements RepresentationModelAssembler<MatchResponse, EntityModel<MatchResponse>> {

    @Override
    public EntityModel<MatchResponse> toModel(MatchResponse match) {
        EntityModel<MatchResponse> model = EntityModel.of(match,
                linkTo(methodOn(MatchController.class).getMatchById(match.getId())).withSelfRel(),
                linkTo(methodOn(MatchController.class).getAllMatches(null, null,  0, 20)).withRel("collection")
        );
        return model;
    }
}

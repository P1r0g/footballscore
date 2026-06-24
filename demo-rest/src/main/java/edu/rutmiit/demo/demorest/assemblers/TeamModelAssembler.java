package edu.rutmiit.demo.demorest.assemblers;

import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.demorest.controllers.TeamController;
import edu.rutmiit.demo.demorest.controllers.MatchController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TeamModelAssembler implements RepresentationModelAssembler<TeamResponse, EntityModel<TeamResponse>> {

    @Override
    public EntityModel<TeamResponse> toModel(TeamResponse team) {
        return EntityModel.of(team,
                linkTo(methodOn(TeamController.class).getTeamById(team.getId())).withSelfRel(),
                linkTo(methodOn(MatchController.class).getAllMatches(team.getId(), null, 0, 20)).withRel("matches"),
                linkTo(methodOn(TeamController.class).getAllTeams(0, 20)).withRel("teams")
        );
    }
}